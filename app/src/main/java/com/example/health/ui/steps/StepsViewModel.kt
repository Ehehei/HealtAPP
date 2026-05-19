package com.example.health.ui.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.DailyStepSummary
import com.example.domain.model.WeeklyStepStats
import com.example.domain.usecase.steps.GetDailyStepSummaryUseCase
import com.example.domain.usecase.steps.GetWeeklyStepStatsUseCase
import com.example.domain.usecase.steps.SyncStepsFromHealthConnectUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class StepsViewModel(
    private val getDaily: GetDailyStepSummaryUseCase,
    private val getWeekly: GetWeeklyStepStatsUseCase,
    private val sync: SyncStepsFromHealthConnectUseCase,
) : ViewModel() {

    private val _today = MutableStateFlow<DailyStepSummary?>(null)
    val today: StateFlow<DailyStepSummary?> = _today.asStateFlow()

    private val _week = MutableStateFlow<WeeklyStepStats?>(null)
    val week: StateFlow<WeeklyStepStats?> = _week.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val today = LocalDate.now()
            _today.value = getDaily(Session.USER_ID, today)
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            _week.value = getWeekly(Session.USER_ID, weekStart)
        }
    }

    fun syncFromHealthConnect() {
        viewModelScope.launch {
            val to = LocalDate.now()
            val from = to.minusDays(30)
            val result = sync(Session.USER_ID, from, to)
            _syncMessage.value = result.fold(
                onSuccess = { "Импортировано записей: $it" },
                onFailure = { it.message ?: "Ошибка синхронизации" },
            )
            refresh()
        }
    }
}
