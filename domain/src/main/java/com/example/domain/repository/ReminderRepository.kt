package com.example.domain.repository

import com.example.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    suspend fun insert(reminder: Reminder): Long
    suspend fun update(reminder: Reminder)
    suspend fun deleteById(id: Long)
    suspend fun setEnabled(id: Long, enabled: Boolean)
    suspend fun getById(id: Long): Reminder?
    suspend fun getEnabledForUser(userId: String): List<Reminder>
    fun observeByUserId(userId: String): Flow<List<Reminder>>
}
