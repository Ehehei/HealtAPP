package com.example.data.repository

import com.example.data.local.dao.StateOfHealthDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.StateOfHealth
import com.example.domain.repository.StateOfHealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class StateOfHealthRepositoryImpl(
    private val dao: StateOfHealthDao,
) : StateOfHealthRepository {

    override suspend fun insert(record: StateOfHealth) {
        dao.insert(record.toEntity())
    }

    override suspend fun update(record: StateOfHealth) = dao.update(record.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun getById(id: Long): StateOfHealth? = dao.getById(id)?.toDomain()

    override suspend fun getByUserId(userId: String): List<StateOfHealth> =
        dao.getByUserId(userId).map { it.toDomain() }

    override suspend fun getByDateRange(
        userId: String,
        from: LocalDate,
        to: LocalDate,
    ): List<StateOfHealth> = dao.getByDateRange(userId, from.toEpochDay(), to.toEpochDay())
        .map { it.toDomain() }

    override fun observeByUserId(userId: String): Flow<List<StateOfHealth>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }
}
