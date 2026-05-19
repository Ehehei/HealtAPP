package com.example.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.domain.repository.MedicationRepository
import com.example.domain.repository.ReminderRepository
import com.example.domain.usecase.medication.LogMedicationIntakeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val reminderRepository: ReminderRepository by inject()
    private val medicationRepository: MedicationRepository by inject()
    private val scheduler: ReminderScheduler by inject()
    private val notifier: ReminderNotifier by inject()
    private val logIntake: LogMedicationIntakeUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_REMINDER_ID, -1L).takeIf { it >= 0 } ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_FIRE -> handleFire(id)
                    ACTION_SNOOZE -> handleSnooze(id)
                    ACTION_TAKEN -> handleTaken(id)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handleFire(id: Long) {
        val reminder = reminderRepository.getById(id) ?: return
        if (!reminder.enabled) return
        val medication = reminder.medicationId?.let { medicationRepository.getById(it) }
        notifier.show(reminder, medication)
        scheduler.schedule(reminder)
    }

    private suspend fun handleSnooze(id: Long) {
        if (reminderRepository.getById(id)?.enabled != true) return
        scheduler.snooze(id, SNOOZE_MINUTES)
    }

    private suspend fun handleTaken(id: Long) {
        val reminder = reminderRepository.getById(id) ?: return
        val medicationId = reminder.medicationId ?: return
        val medication = medicationRepository.getById(medicationId)
        logIntake(
            userId = reminder.userId,
            medicationId = medicationId,
            reminderId = reminder.id,
            dose = reminder.doseOverride ?: medication?.dose,
        )
        notifier.cancel(id)
    }

    companion object {
        const val ACTION_FIRE = "com.example.health.action.FIRE_REMINDER"
        const val ACTION_SNOOZE = "com.example.health.action.SNOOZE_REMINDER"
        const val ACTION_TAKEN = "com.example.health.action.TAKEN_REMINDER"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val SNOOZE_MINUTES = 15
    }
}
