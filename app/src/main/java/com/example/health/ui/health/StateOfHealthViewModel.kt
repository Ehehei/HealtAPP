package com.example.health.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FeelingLevel
import com.example.domain.model.HealthTrend
import com.example.domain.model.StateOfHealth
import com.example.domain.repository.StateOfHealthRepository
import com.example.domain.usecase.health.GetHealthTrendUseCase
import com.example.domain.usecase.health.SaveStateOfHealthUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class StateOfHealthViewModel(
    repository: StateOfHealthRepository,
    private val save: SaveStateOfHealthUseCase,
    private val getTrend: GetHealthTrendUseCase,
) : ViewModel() {

    val history: StateFlow<List<StateOfHealth>> =
        repository.observeByUserId(Session.USER_ID)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _trend = MutableStateFlow<HealthTrend?>(null)
    val trend: StateFlow<HealthTrend?> = _trend.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch { _trend.value = getTrend(Session.USER_ID) }
    }

    fun add(level: FeelingLevel, sugar: Float?, temp: Float?, notes: String?) {
        viewModelScope.launch {
            val r = save(
                StateOfHealth(
                    id = 0,
                    userId = Session.USER_ID,
                    feelingLevel = level,
                    bloodSugar = sugar,
                    temperature = temp,
                    notes = notes,
                    date = LocalDate.now(),
                )
            )
            _error.value = r.exceptionOrNull()?.message
            refresh()
        }
    }
}
