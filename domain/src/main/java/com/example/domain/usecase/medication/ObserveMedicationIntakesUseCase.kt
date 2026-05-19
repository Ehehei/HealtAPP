package com.example.domain.usecase.medication

import com.example.domain.model.MedicationIntakeRecord
import com.example.domain.repository.MedicationIntakeRepository
import kotlinx.coroutines.flow.Flow

class ObserveMedicationIntakesUseCase(
    private val repository: MedicationIntakeRepository,
) {
    operator fun invoke(userId: String, limit: Int = 50): Flow<List<MedicationIntakeRecord>> =
        repository.observeByUserId(userId, limit)
}
