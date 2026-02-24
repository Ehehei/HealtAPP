package com.example.domain.repository

import com.example.domain.model.BloodPressure
import java.time.LocalDateTime

interface BloodPressureRepository {
    suspend fun insert(record: BloodPressure)
    suspend fun update(record: BloodPressure)
    suspend fun deleteById(id: Long)
    suspend fun getById(id: Long): BloodPressure?
    suspend fun getByUserId(userId: String): List<BloodPressure>
    suspend fun getByDateRange(userId: String, from: LocalDateTime, to: LocalDateTime): List<BloodPressure>
}