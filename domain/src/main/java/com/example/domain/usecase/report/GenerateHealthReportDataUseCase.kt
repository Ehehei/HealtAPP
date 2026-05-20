package com.example.domain.usecase.report

import com.example.domain.model.BloodPressureStats
import com.example.domain.model.BpClassification
import com.example.domain.model.HealthReportData
import com.example.domain.model.screening.ScreeningStatus
import com.example.domain.repository.BloodPressureRepository
import com.example.domain.repository.MedicationIntakeRepository
import com.example.domain.repository.MedicationRepository
import com.example.domain.repository.ReminderRepository
import com.example.domain.repository.ScreeningCatalog
import com.example.domain.repository.ScreeningRecordRepository
import com.example.domain.repository.StateOfHealthRepository
import com.example.domain.repository.StepRepository
import com.example.domain.repository.UserProfileRepository
import com.example.domain.repository.WeightRepository
import com.example.domain.usecase.profile.CalculateUserAgeUseCase
import com.example.domain.usecase.screening.GetEligibleScreeningsUseCase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class GenerateHealthReportDataUseCase(
    private val profileRepository: UserProfileRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val weightRepository: WeightRepository,
    private val stepRepository: StepRepository,
    private val healthRepository: StateOfHealthRepository,
    private val medicationRepository: MedicationRepository,
    private val medicationIntakeRepository: MedicationIntakeRepository,
    private val reminderRepository: ReminderRepository,
    private val screeningCatalog: ScreeningCatalog,
    private val screeningRecordRepository: ScreeningRecordRepository,
    private val calculateAge: CalculateUserAgeUseCase,
    private val eligibleScreenings: GetEligibleScreeningsUseCase,
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

        val medications = medicationRepository.getByUserId(userId)
        val medsById = medications.associateBy { it.id }

        val fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val toInstant = to.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        val intakes = medicationIntakeRepository.observeByUserId(userId, limit = 1000).first()
            .filter { it.takenAt in fromInstant..toInstant }
            .sortedByDescending { it.takenAt }

        val activeReminders = reminderRepository.getEnabledForUser(userId)

        val age = calculateAge(profile.birthDate)
        val screeningRecords = screeningRecordRepository.getByUserId(userId)
        val eligibility = eligibleScreenings
            .once(screeningCatalog.all(), age, profile.gender, screeningRecords)
            .filter { it.status != ScreeningStatus.NOT_ELIGIBLE }

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
            bmi = bmi,
            medicationIntakes = intakes,
            medicationsById = medsById,
            activeReminders = activeReminders,
            eligibleScreenings = eligibility,
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
