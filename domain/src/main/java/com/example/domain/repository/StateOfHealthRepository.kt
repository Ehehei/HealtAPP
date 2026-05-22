package com.example.domain.repository

import com.example.domain.model.StateOfHealth
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StateOfHealthRepository {
    suspend fun insert(record: StateOfHealth)
    suspend fun update(record: StateOfHealth)
    suspend fun deleteById(id: Long)
    suspend fun getById(id: Long): StateOfHealth?
    suspend fun getByUserId(userId: String): List<StateOfHealth>
    suspend fun getByDateRange(userId: String, from: LocalDate, to: LocalDate): List<StateOfHealth>
    fun observeByUserId(userId: String): Flow<List<StateOfHealth>>
}
