package com.example.health.ui.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.DailyStepSummary
import com.example.domain.model.WeeklyStepStats
import com.example.domain.repository.HealthConnectDataSource
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
    private val healthConnect: HealthConnectDataSource,
) : ViewModel() {

    private val _today = MutableStateFlow<DailyStepSummary?>(null)
    val today: StateFlow<DailyStepSummary?> = _today.asStateFlow()

    private val _week = MutableStateFlow<WeeklyStepStats?>(null)
    val week: StateFlow<WeeklyStepStats?> = _week.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    /**
     * Набор permission-строк, которые UI передаёт в `PermissionController` контракт.
     * Пустой набор означает, что HC недоступен и запрашивать нечего.
     */
    val healthConnectPermissions: Set<String> = healthConnect.requiredPermissions()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val today = LocalDate.now()
            _today.value = getDaily(Session.USER_ID, today)
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            _week.value = getWeekly(Session.USER_ID, weekStart)
        }
    }

    /**
     * Перед запуском импорта UI проверяет доступность HC и разрешения через этот метод.
     * Возвращает текущее состояние, чтобы экран мог решить: запустить импорт сразу
     * либо сначала открыть системный диалог разрешения.
     */
    suspend fun checkHealthConnectState(): HealthConnectState = when {
        !healthConnect.isAvailable() -> HealthConnectState.NOT_INSTALLED
        !healthConnect.hasReadStepsPermission() -> HealthConnectState.NEEDS_PERMISSION
        else -> HealthConnectState.READY
    }

    fun syncFromHealthConnect() {
        viewModelScope.launch {
            _syncMessage.value = "Импортируем…"
            val to = LocalDate.now()
            val from = to.minusDays(30)
            val result = sync(Session.USER_ID, from, to)
            _syncMessage.value = result.fold(
                onSuccess = { count ->
                    if (count == 0) "Health Connect не вернул записей за 30 дней"
                    else "Импортировано записей: $count"
                },
                onFailure = { it.message ?: "Ошибка синхронизации" },
            )
            refresh()
        }
    }

    fun setMessage(text: String) {
        _syncMessage.value = text
    }
}

enum class HealthConnectState { NOT_INSTALLED, NEEDS_PERMISSION, READY }
