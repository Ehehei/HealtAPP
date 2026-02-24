package com.example.domain.usecase.health

import com.example.domain.model.StateOfHealth
import com.example.domain.repository.StateOfHealthRepository

class SaveStateOfHealthUseCase(
    private val repository: StateOfHealthRepository
) {
    suspend operator fun invoke(record: StateOfHealth): Result<Unit> {
        record.bloodSugar?.let {
            if (it !in 20f..600f) {
                return Result.failure(IllegalArgumentException("Blood sugar out of range (20-600 mg/dL)"))
            }
        }
        record.temperature?.let {
            if (it !in 30f..45f) {
                return Result.failure(IllegalArgumentException("Temperature out of range (30-45 °C)"))
            }
        }

        val existing = repository.getByDateRange(record.userId, record.date, record.date)
        if (existing.isNotEmpty()) {
            repository.update(
                existing.first().copy(
                    feelingLevel = record.feelingLevel,
                    bloodSugar = record.bloodSugar,
                    temperature = record.temperature,
                    notes = record.notes
                )
            )
        } else {
            repository.insert(record)
        }
        return Result.success(Unit)
    }
}
