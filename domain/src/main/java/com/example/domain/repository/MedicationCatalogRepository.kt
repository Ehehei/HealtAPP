package com.example.domain.repository

import com.example.domain.model.MedicationCatalogItem

interface MedicationCatalogRepository {
    val sourceLabel: String
    val sourceUpdatedOn: String
    fun all(): List<MedicationCatalogItem>
    fun search(query: String): List<MedicationCatalogItem>
    fun byInn(inn: String): MedicationCatalogItem?
}
