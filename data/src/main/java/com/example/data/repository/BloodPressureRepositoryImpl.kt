package com.example.data.repository

import com.example.data.local.dao.BloodPressureDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.data.mapper.toEpochMillis
import com.example.domain.model.BloodPressure
import com.example.domain.repository.BloodPressureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class BloodPressureRepositoryImpl(
    private val dao: BloodPressureDao,
) : BloodPressureRepository {

    override suspend fun insert(record: BloodPressure) {
        dao.insert(record.toEntity())
    }

    override suspend fun update(record: BloodPressure) = dao.update(record.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun getById(id: Long): BloodPressure? = dao.getById(id)?.toDomain()

    override suspend fun getByUserId(userId: String): List<BloodPressure> =
        dao.getByUserId(userId).map { it.toDomain() }

    override suspend fun getByDateRange(
        userId: String,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<BloodPressure> = dao.getByDateRange(userId, from.toEpochMillis(), to.toEpochMillis())
        .map { it.toDomain() }

    override fun observeByUserId(userId: String): Flow<List<BloodPressure>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }
}
