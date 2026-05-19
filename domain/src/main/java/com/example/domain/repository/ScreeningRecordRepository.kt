package com.example.domain.repository

import com.example.domain.model.screening.ScreeningRecord
import kotlinx.coroutines.flow.Flow

interface ScreeningRecordRepository {
    suspend fun insert(record: ScreeningRecord): Long
    suspend fun deleteById(id: Long)
    suspend fun getByUserId(userId: String): List<ScreeningRecord>
    fun observeByUserId(userId: String): Flow<List<ScreeningRecord>>
}
