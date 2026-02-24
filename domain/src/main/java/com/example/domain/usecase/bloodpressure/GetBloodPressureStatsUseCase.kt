package com.example.domain.usecase.bloodpressure

import com.example.domain.model.BloodPressureStats
import com.example.domain.model.BpClassification
import com.example.domain.repository.BloodPressureRepository
import java.time.LocalDateTime

class GetBloodPressureStatsUseCase(
    private val repository: BloodPressureRepository
) {
    suspend operator fun invoke(
        userId: String,
        from: LocalDateTime,
        to: LocalDateTime
    ): BloodPressureStats? {
        val records = repository.getByDateRange(userId, from, to)
        if (records.isEmpty()) return null

        val avgSys = records.map { it.systolicPressure }.average().toFloat()
        val avgDia = records.map { it.diastolicPressure }.average().toFloat()
        val avgPulse = records.map { it.pulse }.average().toFloat()

        return BloodPressureStats(
            avgSystolic = avgSys,
            avgDiastolic = avgDia,
            avgPulse = avgPulse,
            maxSystolic = records.maxOf { it.systolicPressure },
            minSystolic = records.minOf { it.systolicPressure },
            maxDiastolic = records.maxOf { it.diastolicPressure },
            minDiastolic = records.minOf { it.diastolicPressure },
            recordCount = records.size,
            classification = classify(avgSys.toInt(), avgDia.toInt())
        )
    }

    private fun classify(systolic: Int, diastolic: Int): BpClassification = when {
        systolic >= 180 || diastolic >= 120 -> BpClassification.HYPERTENSIVE_CRISIS
        systolic >= 140 || diastolic >= 90 -> BpClassification.HYPERTENSION_STAGE_2
        systolic in 130..139 || diastolic in 80..89 -> BpClassification.HYPERTENSION_STAGE_1
        systolic in 120..129 && diastolic < 80 -> BpClassification.ELEVATED
        else -> BpClassification.NORMAL
    }
}
