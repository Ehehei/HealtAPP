package com.example.data.repository

import com.example.data.local.dao.ScreeningRecordDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.screening.ScreeningRecord
import com.example.domain.repository.ScreeningRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScreeningRecordRepositoryImpl(
    private val dao: ScreeningRecordDao,
) : ScreeningRecordRepository {

    override suspend fun insert(record: ScreeningRecord): Long =
        dao.insert(record.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun getByUserId(userId: String): List<ScreeningRecord> =
        dao.getByUserId(userId).map { it.toDomain() }

    override fun observeByUserId(userId: String): Flow<List<ScreeningRecord>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }
}
