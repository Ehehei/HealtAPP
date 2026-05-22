package com.example.domain.repository

import com.example.domain.model.StepRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StepRepository {
    suspend fun insert(record: StepRecord)
    suspend fun update(record: StepRecord)
    suspend fun deleteById(id: Long)
    suspend fun getById(id: Long): StepRecord?
    suspend fun getByUserId(userId: String): List<StepRecord>
    suspend fun getByDateRange(userId: String, from: LocalDate, to: LocalDate): List<StepRecord>
    fun observeByUserId(userId: String): Flow<List<StepRecord>>
}
