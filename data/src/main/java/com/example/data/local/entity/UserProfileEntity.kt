package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val height: Float,
    val initialWeightKg: Float,
    val birthDateEpochDay: Long,
    val gender: String,
    val bloodType: String = "UNKNOWN",
    val allergies: String = "",
    val chronicConditions: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
)
