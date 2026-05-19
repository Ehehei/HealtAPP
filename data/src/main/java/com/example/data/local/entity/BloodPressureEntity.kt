package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_pressure")
data class BloodPressureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val systolicPressure: Int,
    val diastolicPressure: Int,
    val pulse: Int,
    val dateEpochMillis: Long,
)