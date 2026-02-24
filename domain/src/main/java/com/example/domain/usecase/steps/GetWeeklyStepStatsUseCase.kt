package com.example.domain.usecase.steps

import com.example.domain.model.StepRecord
import com.example.domain.model.WeeklyStepStats
import com.example.domain.repository.StepRepository
import java.time.LocalDate

class GetWeeklyStepStatsUseCase(
    private val repository: StepRepository
) {
    suspend operator fun invoke(userId: String, weekStartDate: LocalDate): WeeklyStepStats {
        val weekEnd = weekStartDate.plusDays(6)
        val records = repository.getByDateRange(userId, weekStartDate, weekEnd)
        val recordsByDate = records.associateBy { it.date }

        val fullWeek = (0L..6L).map { dayOffset ->
            val date = weekStartDate.plusDays(dayOffset)
            recordsByDate[date] ?: StepRecord(
                id = 0,
                userId = userId,
                steps = 0,
                date = date
            )
        }

        val goal = GetDailyStepSummaryUseCase.DEFAULT_STEP_GOAL

        return WeeklyStepStats(
            dailySteps = fullWeek,
            totalSteps = fullWeek.sumOf { it.steps },
            averageSteps = if (fullWeek.isNotEmpty()) fullWeek.sumOf { it.steps } / fullWeek.size else 0,
            daysWithGoalReached = fullWeek.count { it.steps >= goal }
        )
    }
}
