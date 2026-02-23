package com.example.domain.model

import java.time.LocalDateTime

data class BodyPhoto(
    val id: Long,
    val userId: String,
    val filePath: String,
    val type: PhotoType,
    val note: String? = null,
    val date: LocalDateTime
)

enum class PhotoType { BODY, ANALYSIS }