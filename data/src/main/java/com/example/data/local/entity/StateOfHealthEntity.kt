package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "state_of_health")
data class StateOfHealthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val feelingLevel: String,
    val bloodSugar: Float?,
    val temperature: Float?,
    val notes: String?,
    val dateEpochDay: Long,
)
