package com.example.domain.model

data class WeightProgress(
    val initialWeightKg: Float,
    val currentWeightKg: Float,
    val changeKg: Float,
    val changePercent: Float,
    val trend: WeightTrend
)

enum class WeightTrend {
    LOSING,
    GAINING,
    STABLE
}
