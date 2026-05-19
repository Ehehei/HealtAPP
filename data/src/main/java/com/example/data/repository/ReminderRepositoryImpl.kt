package com.example.data.repository

import com.example.data.local.dao.ReminderDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.data.notifications.ReminderScheduler
import com.example.domain.model.Reminder
import com.example.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler,
) : ReminderRepository {

    override suspend fun insert(reminder: Reminder): Long {
        val id = dao.insert(reminder.toEntity())
        val saved = reminder.copy(id = id)
        if (saved.enabled) scheduler.schedule(saved)
        return id
    }

    override suspend fun update(reminder: Reminder) {
        dao.update(reminder.toEntity())
        scheduler.cancel(reminder.id)
        if (reminder.enabled) scheduler.schedule(reminder)
    }

    override suspend fun deleteById(id: Long) {
        scheduler.cancel(id)
        dao.deleteById(id)
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        dao.setEnabled(id, enabled)
        if (enabled) {
            dao.getById(id)?.let { scheduler.schedule(it.toDomain()) }
        } else {
            scheduler.cancel(id)
        }
    }

    override suspend fun getById(id: Long): Reminder? = dao.getById(id)?.toDomain()

    override suspend fun getEnabledForUser(userId: String): List<Reminder> =
        dao.getEnabledForUser(userId).map { it.toDomain() }

    override fun observeByUserId(userId: String): Flow<List<Reminder>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }
}
