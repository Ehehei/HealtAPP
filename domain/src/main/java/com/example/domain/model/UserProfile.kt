package com.example.domain.model

import java.time.LocalDate

data class UserProfile(
    val id: String,
    val name: String,
    val height: Float,
    val initialWeightKg: Float,
    val birthDate: LocalDate,
    val gender: Gender
)

enum class Gender {MALE,FEMALE}