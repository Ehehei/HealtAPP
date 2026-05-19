package com.example.data.repository

import com.example.data.local.dao.MedicationDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.Medication
import com.example.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedicationRepositoryImpl(
    private val dao: MedicationDao,
) : MedicationRepository {

    override suspend fun insert(medication: Medication): Long =
        dao.insert(medication.toEntity())

    override suspend fun update(medication: Medication) = dao.update(medication.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun getById(id: Long): Medication? = dao.getById(id)?.toDomain()

    override suspend fun getByUserId(userId: String): List<Medication> =
        dao.getByUserId(userId).map { it.toDomain() }

    override fun observeByUserId(userId: String): Flow<List<Medication>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }
}
