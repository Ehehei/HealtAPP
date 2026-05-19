package com.example.domain.repository

import com.example.domain.model.screening.Screening

/**
 * Источник статической информации о программе скрининга. Реализация в data-слое содержит
 * правила РК с указанием источника. Не зависит от пользователя.
 */
interface ScreeningCatalog {
    val sourceLabel: String
    val sourceUpdatedOn: String
    fun all(): List<Screening>
    fun byCode(code: String): Screening?
}
