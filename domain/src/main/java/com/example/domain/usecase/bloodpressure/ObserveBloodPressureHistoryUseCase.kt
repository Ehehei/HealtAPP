package com.example.domain.usecase.bloodpressure

import com.example.domain.model.BloodPressure
import com.example.domain.repository.BloodPressureRepository
import kotlinx.coroutines.flow.Flow

class ObserveBloodPressureHistoryUseCase(
    private val repository: BloodPressureRepository
) {
    operator fun invoke(userId: String): Flow<List<BloodPressure>> =
        repository.observeByUserId(userId)
}
