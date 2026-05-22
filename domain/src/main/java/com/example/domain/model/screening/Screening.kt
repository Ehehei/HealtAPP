package com.example.domain.model.screening

import com.example.domain.model.Gender

data class Screening(
    val code: String,
    val name: String,
    val description: String,
    val method: String,
    val ageRange: IntRange,
    val eligibleGender: Gender?,
    val intervalMonths: Int,
)
