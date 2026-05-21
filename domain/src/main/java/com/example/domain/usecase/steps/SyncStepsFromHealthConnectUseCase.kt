package com.example.domain.usecase.steps

import com.example.domain.repository.HealthConnectDataSource
import com.example.domain.repository.StepRepository
import java.time.LocalDate

class SyncStepsFromHealthConnectUseCase(
    private val healthConnect: HealthConnectDataSource,
    private val stepRepository: StepRepository,
) {
    suspend operator fun invoke(userId: String, from: LocalDate, to: LocalDate): Result<Int> {
        if (!healthConnect.isAvailable()) {
            return Result.failure(
                IllegalStateException("Health Connect не установлен на устройстве"),
            )
        }
        if (!healthConnect.hasReadStepsPermission()) {
            return Result.failure(
                SecurityException("Нет разрешения на чтение шагов из Health Connect"),
            )
        }

        val hcSteps = healthConnect.getSteps(from, to)
        var syncedCount = 0

        for (hcRecord in hcSteps) {
            val existing = stepRepository.getByDateRange(userId, hcRecord.date, hcRecord.date)
            if (existing.isNotEmpty()) {
                stepRepository.update(existing.first().copy(steps = hcRecord.steps))
            } else {
                stepRepository.insert(hcRecord.copy(userId = userId))
            }
            syncedCount++
        }

        return Result.success(syncedCount)
    }
}
