package com.example.domain.usecase.dashboard

import com.example.domain.model.BmiCategory
import com.example.domain.model.BpClassification
import com.example.domain.model.DashboardSummary
import com.example.domain.repository.BloodPressureRepository
import com.example.domain.repository.StateOfHealthRepository
import com.example.domain.repository.StepRepository
import com.example.domain.repository.UserProfileRepository
import com.example.domain.repository.WeightRepository
import com.example.domain.usecase.steps.GetDailyStepSummaryUseCase
import java.time.LocalDate
import java.time.LocalTime

class GetDashboardSummaryUseCase(
    private val userProfileRepository: UserProfileRepository,
    private val stepRepository: StepRepository,
    private val weightRepository: WeightRepository,
    private val healthRepository: StateOfHealthRepository,
    private val bloodPressureRepository: BloodPressureRepository
) {
    suspend operator fun invoke(userId: String): DashboardSummary? {
        val profile = userProfileRepository.getById(userId) ?: return null
        val today = LocalDate.now()

        val todaySteps = stepRepository.getByDateRange(userId, today, today)
            .firstOrNull()?.steps ?: 0
        val stepGoal = GetDailyStepSummaryUseCase.DEFAULT_STEP_GOAL

        val weights = weightRepository.getByUserId(userId)
        val latestWeight = weights.maxByOrNull { it.date }
        val weightChange = latestWeight?.let { it.weightKg - profile.initialWeightKg }

        val heightM = profile.height / 100f
        val bmi = if (latestWeight != null && heightM > 0f) {
            latestWeight.weightKg / (heightM * heightM)
        } else null
        val bmiCategory = bmi?.let {
            when {
                it < 18.5f -> BmiCategory.UNDERWEIGHT
                it < 25f -> BmiCategory.NORMAL
                it < 30f -> BmiCategory.OVERWEIGHT
                else -> BmiCategory.OBESE
            }
        }

        val todayFeeling = healthRepository.getByDateRange(userId, today, today)
            .firstOrNull()?.feelingLevel

        val todayStart = today.atStartOfDay()
        val todayEnd = today.atTime(LocalTime.MAX)
        val latestBp = bloodPressureRepository.getByDateRange(userId, todayStart, todayEnd)
            .maxByOrNull { it.date }
        val bpClassification = latestBp?.let {
            classifyBp(it.systolicPressure, it.diastolicPressure)
        }

        return DashboardSummary(
            userName = profile.name,
            todaySteps = todaySteps,
            stepGoalPercent = (todaySteps.toFloat() / stepGoal).coerceAtMost(1f),
            latestWeight = latestWeight?.weightKg,
            weightChangeSinceStart = weightChange,
            bmi = bmi,
            bmiCategory = bmiCategory,
            todayFeeling = todayFeeling,
            latestBloodPressure = latestBp,
            bpClassification = bpClassification
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
