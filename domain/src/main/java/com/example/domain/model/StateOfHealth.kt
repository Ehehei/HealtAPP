package com.example.domain.model

import java.time.LocalDate

data class StateOfHealth(
    val id: Long,
    val userId: String,
    val feelingLevel: FeelingLevel,
    val bloodSugar: Float? = null,
    val temperature: Float? = null,
    val notes: String? = null,
    val date: LocalDate
)

enum class FeelingLevel(val value: Int) {
    EXCELLENT(5),
    GOOD(4),
    FAIR(3),
    POOR(2),
    BAD(1)
}
