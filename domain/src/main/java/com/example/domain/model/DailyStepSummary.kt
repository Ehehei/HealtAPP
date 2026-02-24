package com.example.domain.model

data class DailyStepSummary(
    val steps: Int,
    val goalSteps: Int,
    val goalReached: Boolean,
    val percentOfGoal: Float
)
