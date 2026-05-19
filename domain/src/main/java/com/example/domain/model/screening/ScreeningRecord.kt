package com.example.domain.model.screening

import java.time.LocalDate

data class ScreeningRecord(
    val id: Long,
    val userId: String,
    val screeningCode: String,
    val date: LocalDate,
    val notes: String? = null,
)
