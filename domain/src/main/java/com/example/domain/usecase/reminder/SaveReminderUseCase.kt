package com.example.domain.usecase.reminder

import com.example.domain.model.Reminder
import com.example.domain.model.ReminderType
import com.example.domain.repository.ReminderRepository

class SaveReminderUseCase(
    private val repository: ReminderRepository,
) {
    suspend operator fun invoke(reminder: Reminder): Result<Long> {
        if (reminder.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Текст напоминания не задан"))
        }
        if (reminder.type == ReminderType.MEDICATION && reminder.medicationId == null) {
            return Result.failure(IllegalArgumentException("К напоминанию о приёме нужно привязать препарат"))
        }
        return if (reminder.id == 0L) {
            Result.success(repository.insert(reminder))
        } else {
            repository.update(reminder)
            Result.success(reminder.id)
        }
    }
}
