package com.example.health.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.WeightProgress
import com.example.domain.model.WeightRecord
import com.example.domain.repository.WeightRepository
import com.example.domain.usecase.weight.GetWeightProgressUseCase
import com.example.domain.usecase.weight.SaveWeightRecordUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class WeightViewModel(
    private val repository: WeightRepository,
    private val save: SaveWeightRecordUseCase,
    private val getProgress: GetWeightProgressUseCase,
) : ViewModel() {

    val history: StateFlow<List<WeightRecord>> =
        repository.observeByUserId(Session.USER_ID)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _progress = MutableStateFlow<WeightProgress?>(null)
    val progress: StateFlow<WeightProgress?> = _progress.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { refreshProgress() }

    fun refreshProgress() {
        viewModelScope.launch { _progress.value = getProgress(Session.USER_ID) }
    }

    fun add(weightKg: Float, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val res = save(WeightRecord(0, Session.USER_ID, weightKg, date))
            _error.value = res.exceptionOrNull()?.message
            refreshProgress()
        }
    }
}
