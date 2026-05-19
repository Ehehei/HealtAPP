package com.example.health.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BloodType
import com.example.domain.model.Gender
import com.example.domain.model.UserProfile
import com.example.domain.repository.UserProfileRepository
import com.example.domain.usecase.profile.CalculateBmiUseCase
import com.example.domain.usecase.profile.CalculateUserAgeUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ProfileViewModel(
    private val repository: UserProfileRepository,
    private val calculateBmi: CalculateBmiUseCase,
    private val calculateAge: CalculateUserAgeUseCase,
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _bmi = MutableStateFlow<Float?>(null)
    val bmi: StateFlow<Float?> = _bmi.asStateFlow()

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
        }
    }
}
