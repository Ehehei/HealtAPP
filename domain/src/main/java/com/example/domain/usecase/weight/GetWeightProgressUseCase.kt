package com.example.domain.usecase.weight

import com.example.domain.model.WeightProgress
import com.example.domain.model.WeightTrend
import com.example.domain.repository.UserProfileRepository
import com.example.domain.repository.WeightRepository

class GetWeightProgressUseCase(
    private val profileRepository: UserProfileRepository,
    private val weightRepository: WeightRepository
) {
    suspend operator fun invoke(userId: String): WeightProgress? {
        val profile = profileRepository.getById(userId) ?: return null
        val weights = weightRepository.getByUserId(userId)
        if (weights.isEmpty()) return null

        val sorted = weights.sortedBy { it.date }
        val current = sorted.last().weightKg
        val initial = profile.initialWeightKg
        val change = current - initial

        val trend = determineTrend(sorted.takeLast(5).map { it.weightKg })

        return WeightProgress(
            initialWeightKg = initial,
            currentWeightKg = current,
            changeKg = change,
            changePercent = if (initial > 0f) (change / initial) * 100f else 0f,
            trend = trend
        )
    }

    private fun determineTrend(recentWeights: List<Float>): WeightTrend {
        if (recentWeights.size < 2) return WeightTrend.STABLE
        val diff = recentWeights.last() - recentWeights.first()
        return when {
            diff < -0.5f -> WeightTrend.LOSING
            diff > 0.5f -> WeightTrend.GAINING
            else -> WeightTrend.STABLE
        }
    }
}
