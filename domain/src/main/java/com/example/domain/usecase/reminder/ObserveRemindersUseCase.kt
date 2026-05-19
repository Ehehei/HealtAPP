package com.example.domain.usecase.reminder

import com.example.domain.model.Reminder
import com.example.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

class ObserveRemindersUseCase(
    private val repository: ReminderRepository,
) {
    operator fun invoke(userId: String): Flow<List<Reminder>> =
        repository.observeByUserId(userId)
}
