package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_photo")
data class BodyPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val filePath: String,
    val type: String, // BODY / ANALYSIS
    val note: String?,
    val dateEpochMillis: Long,
)
