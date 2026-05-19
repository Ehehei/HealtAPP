package com.example.domain.repository

import com.example.domain.model.MedicationCatalogItem

/**
 * Источник статической информации о препаратах, доступных на рынке РК.
 * Реализация в data-слое содержит зашитый seed с указанием источника
 * (Реестр ЛС РК) и даты сверки.
 */
interface MedicationCatalogRepository {
    val sourceLabel: String
    val sourceUpdatedOn: String
    fun all(): List<MedicationCatalogItem>
    fun search(query: String): List<MedicationCatalogItem>
    fun byInn(inn: String): MedicationCatalogItem?
}
