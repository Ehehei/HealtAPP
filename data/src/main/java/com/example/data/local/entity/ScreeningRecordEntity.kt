package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screening_record",
    indices = [Index("userId"), Index("screeningCode")],
)
data class ScreeningRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val screeningCode: String,
    val dateEpochDay: Long,
    val notes: String?,
)
