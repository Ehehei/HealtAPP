package com.example.data.repository

import com.example.data.local.dao.BodyPhotoDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.data.mapper.toEpochMillis
import com.example.domain.model.BodyPhoto
import com.example.domain.model.PhotoType
import com.example.domain.repository.BodyPhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class BodyPhotoRepositoryImpl(
    private val dao: BodyPhotoDao,
) : BodyPhotoRepository {

    override suspend fun insert(photo: BodyPhoto) {
        dao.insert(photo.toEntity())
    }

    override suspend fun update(photo: BodyPhoto) = dao.update(photo.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun getById(id: Long): BodyPhoto? = dao.getById(id)?.toDomain()

    override suspend fun getByUserId(userId: String): List<BodyPhoto> =
        dao.getByUserId(userId).map { it.toDomain() }

    override suspend fun getByType(userId: String, type: PhotoType): List<BodyPhoto> =
        dao.getByType(userId, type.name).map { it.toDomain() }

    override suspend fun getByDateRange(
        userId: String,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<BodyPhoto> = dao.getByDateRange(userId, from.toEpochMillis(), to.toEpochMillis())
        .map { it.toDomain() }

    override fun observeByUserId(userId: String): Flow<List<BodyPhoto>> =
        dao.observeByUserId(userId).map { list -> list.map { it.toDomain() } }

    override fun observeByType(userId: String, type: PhotoType): Flow<List<BodyPhoto>> =
        dao.observeByType(userId, type.name).map { list -> list.map { it.toDomain() } }
}
