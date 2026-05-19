package com.example.domain.usecase.screening

import com.example.domain.model.screening.ScreeningRecord
import com.example.domain.repository.ScreeningCatalog
import com.example.domain.repository.ScreeningRecordRepository

class LogScreeningUseCase(
    private val catalog: ScreeningCatalog,
    private val repository: ScreeningRecordRepository,
) {
    suspend operator fun invoke(record: ScreeningRecord): Result<Long> {
        if (catalog.byCode(record.screeningCode) == null) {
            return Result.failure(IllegalArgumentException("Неизвестный скрининг: ${record.screeningCode}"))
        }
        return Result.success(repository.insert(record))
    }
}
