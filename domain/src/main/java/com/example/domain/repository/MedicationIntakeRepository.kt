package com.example.domain.repository

import com.example.domain.model.MedicationIntakeRecord
import kotlinx.coroutines.flow.Flow

interface MedicationIntakeRepository {
    suspend fun insert(record: MedicationIntakeRecord): Long
    suspend fun deleteById(id: Long)
    fun observeByUserId(userId: String, limit: Int = 50): Flow<List<MedicationIntakeRecord>>
}
