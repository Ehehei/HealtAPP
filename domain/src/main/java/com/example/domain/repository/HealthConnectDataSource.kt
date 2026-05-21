package com.example.domain.repository

import com.example.domain.model.StepRecord
import java.time.LocalDate

interface HealthConnectDataSource {
    /** Установлен ли Health Connect и доступен ли SDK на устройстве. */
    suspend fun isAvailable(): Boolean

    /** Выданы ли все необходимые разрешения для чтения шагов. */
    suspend fun hasReadStepsPermission(): Boolean

    /**
     * Набор permission-строк для запроса через `PermissionController` из UI-слоя.
     * Возвращает пустой набор, если HC недоступен.
     */
    fun requiredPermissions(): Set<String>

    suspend fun getSteps(from: LocalDate, to: LocalDate): List<StepRecord>
}
