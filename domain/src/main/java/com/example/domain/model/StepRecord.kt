package com.example.domain.model

import java.time.LocalDate

data class StepRecord(
    val id: Long,
    val userId: String,
    val steps: Int,
    val date: LocalDate
)
