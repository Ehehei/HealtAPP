package com.example.domain.usecase.steps

import com.example.domain.model.DailyStepSummary
import com.example.domain.repository.StepRepository
import java.time.LocalDate

class GetDailyStepSummaryUseCase(
    private val repository: StepRepository
) {
    suspend operator fun invoke(userId: String, date: LocalDate): DailyStepSummary {
        val records = repository.getByDateRange(userId, date, date)
        val steps = records.firstOrNull()?.steps ?: 0
        val goal = DEFAULT_STEP_GOAL

        return DailyStepSummary(
            steps = steps,
            goalSteps = goal,
            goalReached = steps >= goal,
            percentOfGoal = (steps.toFloat() / goal).coerceAtMost(1f)
        )
    }

    companion object {
        const val DEFAULT_STEP_GOAL = 10_000
    }
}
