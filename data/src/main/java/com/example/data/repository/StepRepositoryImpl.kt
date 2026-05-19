package com.example.data.repository

import com.example.data.local.dao.StepDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.StepRecord
import com.example.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class StepRepositoryImpl(
    private val dao: StepDao,
) : StepRepository {

    override suspend fun insert(record: StepRecord) {
        dao.insert(record.toEntity())
    }

    override suspend fun update(record: StepRecord) = dao.update(record.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun getById(id: Long): StepRecord? = dao.getById(id)?.toDomain()

    override suspend fun getByUserId(userId: String): List<StepRecord> =
        dao.getByUserId(userId).map { it.toDomain() }

    override suspend fun getByDateRange(
        userId: String,
        from: LocalDate,
        to: LocalDate,
    ): List<StepRecord> = dao.getByDateRange(userId, from.toEpochDay(), to.toEpochDay())
        .map { it.toDomain() }

    override fun observeByUserId(userId: String): Flow<List<StepRecord>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }
}
