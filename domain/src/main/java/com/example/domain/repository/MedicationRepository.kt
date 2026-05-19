package com.example.domain.repository

import com.example.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    suspend fun insert(medication: Medication): Long
    suspend fun update(medication: Medication)
    suspend fun deleteById(id: Long)
    suspend fun getById(id: Long): Medication?
    suspend fun getByUserId(userId: String): List<Medication>
    fun observeByUserId(userId: String): Flow<List<Medication>>
}
