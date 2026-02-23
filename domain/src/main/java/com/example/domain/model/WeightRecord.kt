package com.example.domain.model

import java.time.LocalDate

data class WeightRecord(
    val id: Long,
    val userId: String,
    val weightKg: Float,
    val date: LocalDate,
)