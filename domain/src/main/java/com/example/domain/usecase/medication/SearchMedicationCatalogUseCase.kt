package com.example.domain.usecase.medication

import com.example.domain.model.MedicationCatalogItem
import com.example.domain.repository.MedicationCatalogRepository

/**
 * Возвращает подсказки из каталога для поля «название препарата».
 * Пустой запрос отдаёт весь каталог (для отображения списка при первом фокусе).
 * Не больше 8 подсказок — чтобы не перегружать выпадающий список под клавиатурой.
 */
class SearchMedicationCatalogUseCase(
    private val catalog: MedicationCatalogRepository,
) {
    operator fun invoke(query: String, limit: Int = 8): List<MedicationCatalogItem> {
        val results = if (query.isBlank()) catalog.all() else catalog.search(query)
        return results.take(limit)
    }
}
