package com.example.domain.repository

import com.example.domain.model.StepRecord
import java.time.LocalDate

interface HealthConnectDataSource {

    suspend fun isAvailable(): Boolean

    suspend fun hasReadStepsPermission(): Boolean

    fun requiredPermissions(): Set<String>

    suspend fun getSteps(from: LocalDate, to: LocalDate): List<StepRecord>
}
