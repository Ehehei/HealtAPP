package com.example.domain.repository

import com.example.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun insert(profile: UserProfile)
    suspend fun update(profile: UserProfile)
    suspend fun deleteById(id: String)
    suspend fun getById(id: String): UserProfile?
}