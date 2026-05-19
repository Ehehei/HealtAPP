package com.example.health.ui.pressure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BloodPressure
import com.example.domain.model.BloodPressureStats
import com.example.domain.usecase.bloodpressure.GetBloodPressureStatsUseCase
import com.example.domain.usecase.bloodpressure.ObserveBloodPressureHistoryUseCase
import com.example.domain.usecase.bloodpressure.SaveBloodPressureUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PressureViewModel(
    observeHistory: ObserveBloodPressureHistoryUseCase,
    private val save: SaveBloodPressureUseCase,
    private val statsUseCase: GetBloodPressureStatsUseCase,
) : ViewModel() {

    val history: StateFlow<List<BloodPressure>> =
        observeHistory(Session.USER_ID)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _stats = MutableStateFlow<BloodPressureStats?>(null)
    val stats: StateFlow<BloodPressureStats?> = _stats.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { refreshStats() }

    fun refreshStats() {
        viewModelScope.launch {
            val to = LocalDateTime.now()
            val from = to.minusDays(30)
            _stats.value = statsUseCase(Session.USER_ID, from, to)
        }
    }

    fun add(sys: Int, dia: Int, pulse: Int) {
        viewModelScope.launch {
            val r = save(
                BloodPressure(0, Session.USER_ID, sys, dia, pulse, LocalDateTime.now())
            )
            _error.value = r.exceptionOrNull()?.message
            refreshStats()
        }
    }
}
