package com.example.domain.usecase.weight

import com.example.domain.model.WeightRecord
import com.example.domain.repository.WeightRepository

class SaveWeightRecordUseCase(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(record: WeightRecord): Result<Unit> {
        if (record.weightKg !in 1f..500f) {
            return Result.failure(IllegalArgumentException("Weight out of range (1-500 kg)"))
        }

        val existing = repository.getByDateRange(record.userId, record.date, record.date)
        if (existing.isNotEmpty()) {
            repository.update(existing.first().copy(weightKg = record.weightKg))
        } else {
            repository.insert(record)
        }
        return Result.success(Unit)
    }
}
