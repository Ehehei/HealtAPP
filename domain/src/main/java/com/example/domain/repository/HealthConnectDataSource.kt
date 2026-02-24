package com.example.domain.repository

import com.example.domain.model.StepRecord
import java.time.LocalDate

interface HealthConnectDataSource {
    suspend fun getSteps(from: LocalDate, to: LocalDate): List<StepRecord>
    suspend fun isAvailable(): Boolean
}
