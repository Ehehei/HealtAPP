package com.example.domain.repository

import com.example.domain.model.WeightRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WeightRepository {
    suspend fun insert(record: WeightRecord)
    suspend fun update(record: WeightRecord)
    suspend fun deleteById(id: Long)
    suspend fun getById(id: Long): WeightRecord?
    suspend fun getByUserId(userId: String): List<WeightRecord>
    suspend fun getByDateRange(userId: String, from: LocalDate, to: LocalDate): List<WeightRecord>
    fun observeByUserId(userId: String): Flow<List<WeightRecord>>
}