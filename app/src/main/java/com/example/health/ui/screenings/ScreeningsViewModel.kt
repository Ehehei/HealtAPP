package com.example.health.ui.screenings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.screening.ScreeningEligibility
import com.example.domain.model.screening.ScreeningRecord
import com.example.domain.repository.ScreeningCatalog
import com.example.domain.usecase.screening.GetEligibleScreeningsUseCase
import com.example.domain.usecase.screening.LogScreeningUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CatalogMeta(val sourceLabel: String, val sourceUpdatedOn: String)

class ScreeningsViewModel(
    catalog: ScreeningCatalog,
    getEligible: GetEligibleScreeningsUseCase,
    private val logScreening: LogScreeningUseCase,
) : ViewModel() {

    val meta: CatalogMeta = CatalogMeta(catalog.sourceLabel, catalog.sourceUpdatedOn)

    val items: StateFlow<List<ScreeningEligibility>> =
        getEligible(Session.USER_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun logToday(screeningCode: String) {
        viewModelScope.launch {
            val record = ScreeningRecord(
                id = 0,
                userId = Session.USER_ID,
                screeningCode = screeningCode,
                date = LocalDate.now(),
            )
            logScreening(record).onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}
