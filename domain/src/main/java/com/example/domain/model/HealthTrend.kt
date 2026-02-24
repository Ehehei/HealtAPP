package com.example.domain.model

data class HealthTrend(
    val averageFeeling: Float,
    val feelingDistribution: Map<FeelingLevel, Int>,
    val bloodSugarAvg: Float?,
    val temperatureAvg: Float?,
    val entries: List<StateOfHealth>
)
