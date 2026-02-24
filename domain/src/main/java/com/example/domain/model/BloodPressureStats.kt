package com.example.domain.model

data class BloodPressureStats(
    val avgSystolic: Float,
    val avgDiastolic: Float,
    val avgPulse: Float,
    val maxSystolic: Int,
    val minSystolic: Int,
    val maxDiastolic: Int,
    val minDiastolic: Int,
    val recordCount: Int,
    val classification: BpClassification
)

enum class BpClassification {
    NORMAL,
    ELEVATED,
    HYPERTENSION_STAGE_1,
    HYPERTENSION_STAGE_2,
    HYPERTENSIVE_CRISIS
}
