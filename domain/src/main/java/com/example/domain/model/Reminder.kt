package com.example.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

data class Reminder(
    val id: Long,
    val userId: String,
    val type: ReminderType,
    val title: String,
    val timeOfDay: LocalTime,
    val daysOfWeek: Set<DayOfWeek>,
    val medicationId: Long? = null,
    val doseOverride: String? = null,
    val enabled: Boolean = true,
    val createdAt: Instant = Instant.now(),
)

enum class ReminderType {
    MEDICATION,
    BLOOD_PRESSURE,
    WEIGHT,
    FEELING,
    WATER,
}
