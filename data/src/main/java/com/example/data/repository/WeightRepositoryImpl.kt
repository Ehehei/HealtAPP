package com.example.data.repository

import com.example.data.local.dao.WeightDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.WeightRecord
import com.example.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WeightRepositoryImpl(
    private val dao: WeightDao,
) : WeightRepository {

    override suspend fun insert(record: WeightRecord) {
        dao.insert(record.toEntity())
    }

    override suspend fun update(record: WeightRecord) = dao.update(record.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun getById(id: Long): WeightRecord? = dao.getById(id)?.toDomain()

    override suspend fun getByUserId(userId: String): List<WeightRecord> =
        dao.getByUserId(userId).map { it.toDomain() }

    override suspend fun getByDateRange(
        userId: String,
        from: LocalDate,
        to: LocalDate,
    ): List<WeightRecord> = dao.getByDateRange(userId, from.toEpochDay(), to.toEpochDay())
        .map { it.toDomain() }

    override fun observeByUserId(userId: String): Flow<List<WeightRecord>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }
}
