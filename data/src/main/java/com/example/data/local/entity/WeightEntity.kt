package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_record")
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val weightKg: Float,
    val dateEpochDay: Long,
)
