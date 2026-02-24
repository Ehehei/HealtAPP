package com.example.domain.usecase.profile

import com.example.domain.model.BmiCategory
import com.example.domain.model.BmiResult
import com.example.domain.repository.UserProfileRepository
import com.example.domain.repository.WeightRepository

class CalculateBmiUseCase(
    private val profileRepository: UserProfileRepository,
    private val weightRepository: WeightRepository
) {
    suspend operator fun invoke(userId: String): BmiResult? {
        val profile = profileRepository.getById(userId) ?: return null
        val weights = weightRepository.getByUserId(userId)
        val latestWeight = weights.maxByOrNull { it.date } ?: return null

        val heightM = profile.height / 100f
        if (heightM <= 0f) return null

        val bmi = latestWeight.weightKg / (heightM * heightM)

        val category = when {
            bmi < 18.5f -> BmiCategory.UNDERWEIGHT
            bmi < 25f -> BmiCategory.NORMAL
            bmi < 30f -> BmiCategory.OVERWEIGHT
            else -> BmiCategory.OBESE
        }

        return BmiResult(value = bmi, category = category)
    }
}
