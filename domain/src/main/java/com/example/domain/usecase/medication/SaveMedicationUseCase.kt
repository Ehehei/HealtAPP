package com.example.domain.usecase.medication

import com.example.domain.model.Medication
import com.example.domain.repository.MedicationRepository

class SaveMedicationUseCase(
    private val repository: MedicationRepository,
) {
    suspend operator fun invoke(medication: Medication): Result<Long> {
        if (medication.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Название препарата не может быть пустым"))
        }
        if (medication.dose.isBlank()) {
            return Result.failure(IllegalArgumentException("Дозировка не указана"))
        }
        return if (medication.id == 0L) {
            Result.success(repository.insert(medication))
        } else {
            repository.update(medication)
            Result.success(medication.id)
        }
    }
}
