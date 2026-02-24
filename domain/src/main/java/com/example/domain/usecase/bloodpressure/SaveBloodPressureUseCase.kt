package com.example.domain.usecase.bloodpressure

import com.example.domain.model.BloodPressure
import com.example.domain.repository.BloodPressureRepository

class SaveBloodPressureUseCase(
    private val repository: BloodPressureRepository
) {
    suspend operator fun invoke(record: BloodPressure): Result<Unit> {
        if (record.systolicPressure <= record.diastolicPressure) {
            return Result.failure(IllegalArgumentException("Systolic must be greater than diastolic"))
        }
        if (record.systolicPressure !in 60..300) {
            return Result.failure(IllegalArgumentException("Systolic pressure out of range (60-300)"))
        }
        if (record.diastolicPressure !in 30..200) {
            return Result.failure(IllegalArgumentException("Diastolic pressure out of range (30-200)"))
        }
        if (record.pulse !in 30..250) {
            return Result.failure(IllegalArgumentException("Pulse out of range (30-250)"))
        }
        repository.insert(record)
        return Result.success(Unit)
    }
}
