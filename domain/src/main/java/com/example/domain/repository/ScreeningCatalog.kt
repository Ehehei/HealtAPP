package com.example.domain.repository

import com.example.domain.model.screening.Screening

interface ScreeningCatalog {
    val sourceLabel: String
    val sourceUpdatedOn: String
    fun all(): List<Screening>
    fun byCode(code: String): Screening?
}
