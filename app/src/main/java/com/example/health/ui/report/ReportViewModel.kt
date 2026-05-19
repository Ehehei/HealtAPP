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

    fun generate(periodDays: Int = 30) {
        viewModelScope.launch {
            val to = LocalDate.now()
            val from = to.minusDays(periodDays.toLong())
            val data = generateData(Session.USER_ID, from, to)
            if (data == null) {
                _error.value = "Сначала заполни профиль"
                return@launch
            }
            _pdfUri.value = pdf.generate(data)
        }
    }

    fun consume() { _pdfUri.value = null }
}
