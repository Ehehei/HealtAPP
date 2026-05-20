package com.example.health.ui.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.report.PdfReportGenerator
import com.example.domain.usecase.report.GenerateHealthReportDataUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReportViewModel(
    private val generateData: GenerateHealthReportDataUseCase,
    private val pdf: PdfReportGenerator,
) : ViewModel() {

    private val _pdfUri = MutableStateFlow<Uri?>(null)
    val pdfUri: StateFlow<Uri?> = _pdfUri.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _periodDays = MutableStateFlow(30)
    val periodDays: StateFlow<Int> = _periodDays.asStateFlow()

    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating.asStateFlow()

    fun setPeriod(days: Int) { _periodDays.value = days }

    fun generate() {
        if (_generating.value) return
        viewModelScope.launch {
            _generating.value = true
            try {
                val to = LocalDate.now()
                val from = to.minusDays(_periodDays.value.toLong())
                val data = generateData(Session.USER_ID, from, to)
                if (data == null) {
                    _error.value = "Сначала заполни профиль"
                    return@launch
                }
                _error.value = null
                _pdfUri.value = pdf.generate(data)
            } finally {
                _generating.value = false
            }
        }
    }

    fun consume() { _pdfUri.value = null }
}
