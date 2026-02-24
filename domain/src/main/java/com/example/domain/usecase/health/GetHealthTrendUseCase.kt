package com.example.domain.usecase.health

import com.example.domain.model.FeelingLevel
import com.example.domain.model.HealthTrend
import com.example.domain.repository.StateOfHealthRepository
import java.time.LocalDate

class GetHealthTrendUseCase(
    private val repository: StateOfHealthRepository
) {
    suspend operator fun invoke(userId: String, days: Int = 30): HealthTrend {
        val to = LocalDate.now()
        val from = to.minusDays(days.toLong())
        val entries = repository.getByDateRange(userId, from, to)

        val avgFeeling = if (entries.isNotEmpty()) {
            entries.map { it.feelingLevel.value }.average().toFloat()
        } else 0f

        val distribution = entries.groupingBy { it.feelingLevel }.eachCount()

        val sugarValues = entries.mapNotNull { it.bloodSugar }
        val tempValues = entries.mapNotNull { it.temperature }

        return HealthTrend(
            averageFeeling = avgFeeling,
            feelingDistribution = distribution,
            bloodSugarAvg = sugarValues.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            temperatureAvg = tempValues.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            entries = entries
        )
    }
}
