package com.example.domain.repository

import com.example.domain.model.BodyPhoto
import com.example.domain.model.PhotoType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface BodyPhotoRepository {
    suspend fun insert(photo: BodyPhoto)
    suspend fun update(photo: BodyPhoto)
    suspend fun deleteById(id: Long)
    suspend fun getById(id: Long): BodyPhoto?
    suspend fun getByUserId(userId: String): List<BodyPhoto>
    suspend fun getByType(userId: String, type: PhotoType): List<BodyPhoto>
    suspend fun getByDateRange(userId: String, from: LocalDateTime, to: LocalDateTime): List<BodyPhoto>
    fun observeByUserId(userId: String): Flow<List<BodyPhoto>>
    fun observeByType(userId: String, type: PhotoType): Flow<List<BodyPhoto>>
}