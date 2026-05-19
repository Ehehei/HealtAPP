package com.example.domain.model

import java.time.Instant

/**
 * Запись о фактически принятой дозе препарата. Создаётся либо тапом «Принял»
 * на уведомлении, либо вручную из истории. `reminderId` хранится для связи
 * с конкретным напоминанием (опционально — приём вне расписания тоже валиден).
 */
data class MedicationIntakeRecord(
    val id: Long,
    val userId: String,
    val medicationId: Long,
    val takenAt: Instant,
    val reminderId: Long? = null,
    val dose: String? = null,
)
