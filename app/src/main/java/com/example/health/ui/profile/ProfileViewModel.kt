package com.example.health.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BloodType
import com.example.domain.model.Gender
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderType
import com.example.domain.model.UserProfile
import com.example.domain.model.screening.ScreeningStatus
import com.example.domain.repository.UserProfileRepository
import com.example.domain.usecase.medication.ObserveMedicationsUseCase
import com.example.domain.usecase.profile.CalculateBmiUseCase
import com.example.domain.usecase.profile.CalculateUserAgeUseCase
import com.example.domain.usecase.reminder.ObserveRemindersUseCase
import com.example.domain.usecase.screening.GetEligibleScreeningsUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class NextMedReminder(val medicationName: String, val timeOfDay: LocalTime)

class ProfileViewModel(
    private val repository: UserProfileRepository,
    private val calculateBmi: CalculateBmiUseCase,
    private val calculateAge: CalculateUserAgeUseCase,
    observeReminders: ObserveRemindersUseCase,
    observeMedications: ObserveMedicationsUseCase,
    getEligibleScreenings: GetEligibleScreeningsUseCase,
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _bmi = MutableStateFlow<Float?>(null)
    val bmi: StateFlow<Float?> = _bmi.asStateFlow()

    val nextMedReminder: StateFlow<NextMedReminder?> = combine(
        observeReminders(Session.USER_ID),
        observeMedications(Session.USER_ID),
    ) { reminders, meds ->
        val medsById = meds.associateBy { it.id }
        reminders
            .asSequence()
            .filter { it.enabled && it.type == ReminderType.MEDICATION }
            .sortedBy { it.timeOfDay }
            .firstOrNull()
            ?.let { r: Reminder ->
                val name = r.medicationId?.let { medsById[it]?.name } ?: r.title
                NextMedReminder(name, r.timeOfDay)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val screeningsPending: StateFlow<Boolean> =
        getEligibleScreenings(Session.USER_ID)
            .map { list -> list.any { it.status == ScreeningStatus.DUE_NOW } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            _profile.value = repository.getById(Session.USER_ID)
            _bmi.value = calculateBmi(Session.USER_ID)?.value
        }
    }

    fun age(): Int? = _profile.value?.let { calculateAge(it.birthDate) }

    fun save(
        name: String,
        heightCm: Float,
        initialWeightKg: Float,
        birthDate: LocalDate,
        gender: Gender,
        bloodType: BloodType,
        allergies: String,
        chronicConditions: String,
        emergencyContactName: String,
        emergencyContactPhone: String,
    ) {
        viewModelScope.launch {
            val p = UserProfile(
                id = Session.USER_ID,
                name = name,
                height = heightCm,
                initialWeightKg = initialWeightKg,
                birthDate = birthDate,
                gender = gender,
                bloodType = bloodType,
                allergies = allergies,
                chronicConditions = chronicConditions,
                emergencyContactName = emergencyContactName,
                emergencyContactPhone = emergencyContactPhone,
            )
            repository.insert(p)
            _profile.value = p
            _bmi.value = calculateBmi(Session.USER_ID)?.value
        }
    }
}
