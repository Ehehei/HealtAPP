package com.example.domain.model

import java.time.Instant

data class MedicationIntakeRecord(
    val id: Long,
    val userId: String,
    val medicationId: Long,
    val takenAt: Instant,
    val reminderId: Long? = null,
    val dose: String? = null,
)
