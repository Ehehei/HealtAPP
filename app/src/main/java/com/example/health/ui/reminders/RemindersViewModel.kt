package com.example.health.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Medication
import com.example.domain.model.MedicationCatalogItem
import com.example.domain.model.MedicationForm
import com.example.domain.model.MedicationIntakeRecord
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderType
import com.example.domain.repository.MedicationCatalogRepository
import com.example.domain.usecase.medication.DeleteMedicationUseCase
import com.example.domain.usecase.medication.ObserveMedicationIntakesUseCase
import com.example.domain.usecase.medication.ObserveMedicationsUseCase
import com.example.domain.usecase.medication.SaveMedicationUseCase
import com.example.domain.usecase.medication.SearchMedicationCatalogUseCase
import com.example.domain.usecase.reminder.DeleteReminderUseCase
import com.example.domain.usecase.reminder.ObserveRemindersUseCase
import com.example.domain.usecase.reminder.SaveReminderUseCase
import com.example.domain.usecase.reminder.ToggleReminderUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class RemindersViewModel(
    observeMedications: ObserveMedicationsUseCase,
    observeReminders: ObserveRemindersUseCase,
    observeIntakes: ObserveMedicationIntakesUseCase,
    private val saveMedication: SaveMedicationUseCase,
    private val deleteMedication: DeleteMedicationUseCase,
    private val saveReminder: SaveReminderUseCase,
    private val toggleReminder: ToggleReminderUseCase,
    private val deleteReminder: DeleteReminderUseCase,
    private val searchCatalog: SearchMedicationCatalogUseCase,
    catalog: MedicationCatalogRepository,
) : ViewModel() {

    val catalogSourceLabel: String = catalog.sourceLabel
    val catalogSourceUpdatedOn: String = catalog.sourceUpdatedOn

    val medications: StateFlow<List<Medication>> =
        observeMedications(Session.USER_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reminders: StateFlow<List<Reminder>> =
        observeReminders(Session.USER_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val intakes: StateFlow<List<MedicationIntakeRecord>> =
        observeIntakes(Session.USER_ID, limit = 10)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun addMedication(
        name: String,
        dose: String,
        form: MedicationForm,
        instructions: String?,
        registeredInKz: Boolean = false,
    ) {
        viewModelScope.launch {
            val med = Medication(
                id = 0,
                userId = Session.USER_ID,
                name = name.trim(),
                dose = dose.trim(),
                form = form,
                instructions = instructions?.trim()?.takeIf { it.isNotEmpty() },
                registeredInKz = registeredInKz,
            )
            saveMedication(med).onFailure { _error.value = it.message }
        }
    }

    fun suggest(query: String): List<MedicationCatalogItem> = searchCatalog(query)

    fun removeMedication(id: Long) {
        viewModelScope.launch { deleteMedication(id) }
    }

    fun addMedicationReminder(
        medicationId: Long,
        time: LocalTime,
        days: Set<DayOfWeek>,
        doseOverride: String?,
    ) {
        val med = medications.value.firstOrNull { it.id == medicationId }
        if (med == null) {
            _error.value = "Сначала добавь препарат"
            return
        }
        viewModelScope.launch {
            val reminder = Reminder(
                id = 0,
                userId = Session.USER_ID,
                type = ReminderType.MEDICATION,
                title = "Принять ${med.name}",
                timeOfDay = time,
                daysOfWeek = days,
                medicationId = medicationId,
                doseOverride = doseOverride?.trim()?.takeIf { it.isNotEmpty() },
                enabled = true,
            )
            saveReminder(reminder).onFailure { _error.value = it.message }
        }
    }

    fun addGeneralReminder(type: ReminderType, time: LocalTime, days: Set<DayOfWeek>) {
        if (type == ReminderType.MEDICATION) {
            _error.value = "Для лекарства добавь препарат и используй «Напомнить»"
            return
        }
        viewModelScope.launch {
            val reminder = Reminder(
                id = 0,
                userId = Session.USER_ID,
                type = type,
                title = defaultTitle(type),
                timeOfDay = time,
                daysOfWeek = days,
                enabled = true,
            )
            saveReminder(reminder).onFailure { _error.value = it.message }
        }
    }

    fun updateReminderSchedule(reminder: Reminder, time: LocalTime, days: Set<DayOfWeek>) {
        viewModelScope.launch {
            val updated = reminder.copy(timeOfDay = time, daysOfWeek = days)
            saveReminder(updated).onFailure { _error.value = it.message }
        }
    }

    private fun defaultTitle(type: ReminderType): String = when (type) {
        ReminderType.BLOOD_PRESSURE -> "Измерить давление"
        ReminderType.WEIGHT -> "Взвеситься"
        ReminderType.FEELING -> "Отметить самочувствие"
        ReminderType.WATER -> "Выпить воды"
        ReminderType.MEDICATION -> "Принять лекарство"
    }

    fun toggle(id: Long, enabled: Boolean) {
        viewModelScope.launch { toggleReminder(id, enabled) }
    }

    fun remove(id: Long) {
        viewModelScope.launch { deleteReminder(id) }
    }

    fun clearError() { _error.value = null }
}
