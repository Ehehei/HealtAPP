package com.example.domain.usecase.report

import com.example.domain.model.BloodPressureStats
import com.example.domain.model.BpClassification
import com.example.domain.model.HealthReportData
import com.example.domain.repository.BloodPressureRepository
import com.example.domain.repository.StateOfHealthRepository
import com.example.domain.repository.StepRepository
import com.example.domain.repository.UserProfileRepository
import com.example.domain.repository.WeightRepository
import java.time.LocalDate
import java.time.LocalTime

class GenerateHealthReportDataUseCase(
    private val profileRepository: UserProfileRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val weightRepository: WeightRepository,
    private val stepRepository: StepRepository,
    private val healthRepository: StateOfHealthRepository
) {
    suspend operator fun invoke(
        userId: String,
        from: LocalDate,
        to: LocalDate
    ): HealthReportData? {
        val profile = profileRepository.getById(userId) ?: return null

        val fromDateTime = from.atStartOfDay()
        val toDateTime = to.atTime(LocalTime.MAX)

        val bpRecords = bloodPressureRepository.getByDateRange(userId, fromDateTime, toDateTime)
        val bpStats = if (bpRecords.isNotEmpty()) {
            val avgSys = bpRecords.map { it.systolicPressure }.average().toFloat()
            val avgDia = bpRecords.map { it.diastolicPressure }.average().toFloat()
            BloodPressureStats(
                avgSystolic = avgSys,
                avgDiastolic = avgDia,
                avgPulse = bpRecords.map { it.pulse }.average().toFloat(),
                maxSystolic = bpRecords.maxOf { it.systolicPressure },
                minSystolic = bpRecords.minOf { it.systolicPressure },
                maxDiastolic = bpRecords.maxOf { it.diastolicPressure },
                minDiastolic = bpRecords.minOf { it.diastolicPressure },
                recordCount = bpRecords.size,
                classification = classifyBp(avgSys.toInt(), avgDia.toInt())
            )
        } else null

        val weightRecords = weightRepository.getByDateRange(userId, from, to)
        val weightChange = if (weightRecords.size >= 2) {
            val sorted = weightRecords.sortedBy { it.date }
            sorted.last().weightKg - sorted.first().weightKg
        } else null

        val stepRecords = stepRepository.getByDateRange(userId, from, to)
        val avgSteps = if (stepRecords.isNotEmpty()) {
            stepRecords.sumOf { it.steps } / stepRecords.size
        } else 0

        val healthEntries = healthRepository.getByDateRange(userId, from, to)
        val avgFeeling = healthEntries.takeIf { it.isNotEmpty() }
            ?.map { it.feelingLevel.value }?.average()?.toFloat()
        val sugarValues = healthEntries.mapNotNull { it.bloodSugar }
        val tempValues = healthEntries.mapNotNull { it.temperature }

        val latestWeight = weightRepository.getByUserId(userId).maxByOrNull { it.date }
        val heightM = profile.height / 100f
        val bmi = if (latestWeight != null && heightM > 0f) {
            latestWeight.weightKg / (heightM * heightM)
        } else null

        return HealthReportData(
            profile = profile,
            period = from..to,
            bloodPressureRecords = bpRecords,
            bloodPressureStats = bpStats,
            weightRecords = weightRecords,
            weightChange = weightChange,
            stepRecords = stepRecords,
            avgDailySteps = avgSteps,
            healthEntries = healthEntries,
            avgFeeling = avgFeeling,
            bloodSugarAvg = sugarValues.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            temperatureAvg = tempValues.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            bmi = bmi
        )
    }

    private fun classifyBp(systolic: Int, diastolic: Int): BpClassification = when {
        systolic >= 180 || diastolic >= 120 -> BpClassification.HYPERTENSIVE_CRISIS
        systolic >= 140 || diastolic >= 90 -> BpClassification.HYPERTENSION_STAGE_2
        systolic in 130..139 || diastolic in 80..89 -> BpClassification.HYPERTENSION_STAGE_1
        systolic in 120..129 && diastolic < 80 -> BpClassification.ELEVATED
        else -> BpClassification.NORMAL
    }
}
