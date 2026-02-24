package com.example.domain.usecase.profile

import com.example.domain.model.UserProfile
import com.example.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    operator fun invoke(userId: String): Flow<UserProfile?> =
        repository.observeById(userId)
}
