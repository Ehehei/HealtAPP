package com.example.domain.model

import java.time.LocalDateTime

data class BloodPressure(
    val id: Long,
    val userId: String,
    val systolicPressure: Int,
    val diastolicPressure: Int,
    val pulse: Int,
    val date: LocalDateTime
)
