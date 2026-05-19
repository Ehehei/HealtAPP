package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_record")
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val steps: Int,
    val dateEpochDay: Long,
)
