package com.example.data.repository

import com.example.data.local.dao.UserProfileDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.UserProfile
import com.example.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepositoryImpl(
    private val dao: UserProfileDao,
) : UserProfileRepository {

    override suspend fun insert(profile: UserProfile) = dao.insert(profile.toEntity())
    override suspend fun update(profile: UserProfile) = dao.update(profile.toEntity())
    override suspend fun deleteById(id: String) = dao.deleteById(id)
    override suspend fun getById(id: String): UserProfile? = dao.getById(id)?.toDomain()
    override fun observeById(id: String): Flow<UserProfile?> =
        dao.observeById(id).map { it?.toDomain() }
}
