package com.example.data.repository

import com.example.data.local.dao.MedicationIntakeDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.MedicationIntakeRecord
import com.example.domain.repository.MedicationIntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedicationIntakeRepositoryImpl(
    private val dao: MedicationIntakeDao,
) : MedicationIntakeRepository {

    override suspend fun insert(record: MedicationIntakeRecord): Long =
        dao.insert(record.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override fun observeByUserId(userId: String, limit: Int): Flow<List<MedicationIntakeRecord>> =
        dao.observeByUserId(userId, limit).map { list -> list.map { it.toDomain() } }
}
