package com.example.domain.usecase.medication

import com.example.domain.model.MedicationIntakeRecord
import com.example.domain.repository.MedicationIntakeRepository
import java.time.Instant

class LogMedicationIntakeUseCase(
    private val repository: MedicationIntakeRepository,
) {
    suspend operator fun invoke(
        userId: String,
        medicationId: Long,
        reminderId: Long? = null,
        dose: String? = null,
        takenAt: Instant = Instant.now(),
    ): Long = repository.insert(
        MedicationIntakeRecord(
            id = 0,
            userId = userId,
            medicationId = medicationId,
            takenAt = takenAt,
            reminderId = reminderId,
            dose = dose,
        )
    )
}
