package com.example.domain.model

data class WeeklyStepStats(
    val dailySteps: List<StepRecord>,
    val totalSteps: Int,
    val averageSteps: Int,
    val daysWithGoalReached: Int
)
