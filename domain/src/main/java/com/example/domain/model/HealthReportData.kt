package com.example.domain.model

import java.time.LocalDate

data class HealthReportData(
    val profile: UserProfile,
    val period: ClosedRange<LocalDate>,
    val bloodPressureRecords: List<BloodPressure>,
    val bloodPressureStats: BloodPressureStats?,
    val weightRecords: List<WeightRecord>,
    val weightChange: Float?,
    val stepRecords: List<StepRecord>,
    val avgDailySteps: Int,
    val healthEntries: List<StateOfHealth>,
    val avgFeeling: Float?,
    val bloodSugarAvg: Float?,
    val temperatureAvg: Float?,
    val bmi: Float?
)
