package com.example.domain.model

import java.time.LocalDate

data class UserProfile(
    val id: String,
    val name: String,
    val height: Float,
    val initialWeightKg: Float,
    val birthDate: LocalDate,
    val gender: Gender,
    val bloodType: BloodType = BloodType.UNKNOWN,
    val allergies: String = "",
    val chronicConditions: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
)

enum class Gender {MALE,FEMALE}
