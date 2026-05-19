package com.example.domain.usecase.medication

import com.example.domain.repository.MedicationRepository

class DeleteMedicationUseCase(
    private val repository: MedicationRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteById(id)
    }
}
