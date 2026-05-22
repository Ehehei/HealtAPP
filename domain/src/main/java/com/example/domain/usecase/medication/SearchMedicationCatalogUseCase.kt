package com.example.domain.usecase.medication

import com.example.domain.model.MedicationCatalogItem
import com.example.domain.repository.MedicationCatalogRepository

class SearchMedicationCatalogUseCase(
    private val catalog: MedicationCatalogRepository,
) {
    operator fun invoke(query: String, limit: Int = 8): List<MedicationCatalogItem> {
        val results = if (query.isBlank()) catalog.all() else catalog.search(query)
        return results.take(limit)
    }
}
