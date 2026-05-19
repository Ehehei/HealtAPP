package com.example.domain.usecase.reminder

import com.example.domain.repository.ReminderRepository

class DeleteReminderUseCase(
    private val repository: ReminderRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteById(id)
    }
}
