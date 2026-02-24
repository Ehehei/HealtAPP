package com.example.domain.model

data class DashboardSummary(
    val userName: String,
    val todaySteps: Int,
    val stepGoalPercent: Float,
    val latestWeight: Float?,
    val weightChangeSinceStart: Float?,
    val bmi: Float?,
    val bmiCategory: BmiCategory?,
    val todayFeeling: FeelingLevel?,
    val latestBloodPressure: BloodPressure?,
    val bpClassification: BpClassification?
)
