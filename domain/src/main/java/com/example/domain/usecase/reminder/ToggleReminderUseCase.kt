package com.example.domain.usecase.reminder

import com.example.domain.repository.ReminderRepository

class ToggleReminderUseCase(
    private val repository: ReminderRepository,
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) {
        repository.setEnabled(id, enabled)
    }
}
