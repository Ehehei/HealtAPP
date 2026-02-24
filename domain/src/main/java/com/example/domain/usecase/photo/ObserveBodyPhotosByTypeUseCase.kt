package com.example.domain.usecase.photo

import com.example.domain.model.BodyPhoto
import com.example.domain.model.PhotoType
import com.example.domain.repository.BodyPhotoRepository
import kotlinx.coroutines.flow.Flow

class ObserveBodyPhotosByTypeUseCase(
    private val repository: BodyPhotoRepository
) {
    operator fun invoke(userId: String, type: PhotoType): Flow<List<BodyPhoto>> =
        repository.observeByType(userId, type)
}
