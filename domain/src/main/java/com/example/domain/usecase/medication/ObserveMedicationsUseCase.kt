package com.example.domain.usecase.medication

import com.example.domain.model.Medication
import com.example.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow

class ObserveMedicationsUseCase(
    private val repository: MedicationRepository,
) {
    operator fun invoke(userId: String): Flow<List<Medication>> =
        repository.observeByUserId(userId)
}
