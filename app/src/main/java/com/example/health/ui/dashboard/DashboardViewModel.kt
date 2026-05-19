package com.example.health.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.DashboardSummary
import com.example.domain.model.screening.ScreeningStatus
import com.example.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.example.domain.usecase.reminder.ObserveRemindersUseCase
import com.example.domain.usecase.screening.GetEligibleScreeningsUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel(
    private val getDashboard: GetDashboardSummaryUseCase,
    getEligibleScreenings: GetEligibleScreeningsUseCase,
    observeReminders: ObserveRemindersUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardSummary?>(null)
    val state: StateFlow<DashboardSummary?> = _state.asStateFlow()

    val pendingScreenings: StateFlow<Int> =
        getEligibleScreenings(Session.USER_ID)
            .map { list -> list.count { it.status == ScreeningStatus.DUE_NOW } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val todayReminders: StateFlow<Int> =
        observeReminders(Session.USER_ID)
            .map { reminders ->
                val today = LocalDate.now().dayOfWeek
                reminders.count { r ->
                    r.enabled && (r.daysOfWeek.isEmpty() || today in r.daysOfWeek)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = getDashboard(Session.USER_ID)
        }
    }
}
