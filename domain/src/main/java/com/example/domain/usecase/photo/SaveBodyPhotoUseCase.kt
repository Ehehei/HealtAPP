package com.example.domain.usecase.photo

import com.example.domain.model.BodyPhoto
import com.example.domain.repository.BodyPhotoRepository

class SaveBodyPhotoUseCase(
    private val repository: BodyPhotoRepository
) {
    suspend operator fun invoke(photo: BodyPhoto): Result<Unit> {
        if (photo.filePath.isBlank()) {
            return Result.failure(IllegalArgumentException("File path must not be blank"))
        }
        repository.insert(photo)
        return Result.success(Unit)
    }
}
