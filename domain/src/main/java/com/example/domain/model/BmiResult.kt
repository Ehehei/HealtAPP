package com.example.domain.model

data class BmiResult(
    val value: Float,
    val category: BmiCategory
)

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
    OBESE
}
