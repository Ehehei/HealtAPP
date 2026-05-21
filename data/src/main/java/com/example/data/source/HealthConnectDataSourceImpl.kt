package com.example.data.source

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.domain.model.StepRecord
import com.example.domain.repository.HealthConnectDataSource
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectDataSourceImpl(
    private val context: Context,
) : HealthConnectDataSource {

    /**
     * Создаём клиента лениво и безопасно: `HealthConnectClient.getOrCreate` бросает
     * `IllegalStateException`, если HC не установлен, — заворачиваем в Result, чтобы
     * вызывающий код мог корректно деградировать вместо падения приложения.
     */
    private val client: HealthConnectClient? by lazy {
        runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull()
    }

    private val readStepsPermissions: Set<String> =
        setOf(HealthPermission.getReadPermission(StepsRecord::class))

    override suspend fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE &&
            client != null

    override suspend fun hasReadStepsPermission(): Boolean {
        val c = client ?: return false
        return runCatching {
            c.permissionController.getGrantedPermissions().containsAll(readStepsPermissions)
        }.getOrDefault(false)
    }

    override fun requiredPermissions(): Set<String> = readStepsPermissions

    override suspend fun getSteps(from: LocalDate, to: LocalDate): List<StepRecord> {
        val c = client ?: return emptyList()
        val zone = ZoneId.systemDefault()
        val start = from.atStartOfDay(zone).toInstant()
        val end = to.plusDays(1).atStartOfDay(zone).toInstant()

        val response = runCatching {
            c.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
        }.getOrElse { return emptyList() }

        return response.records
            .groupBy { it.startTime.atZone(zone).toLocalDate() }
            .map { (date, records) ->
                StepRecord(
                    id = 0,
                    userId = "",
                    steps = records.sumOf { it.count }.toInt(),
                    date = date,
                )
            }
    }
}
