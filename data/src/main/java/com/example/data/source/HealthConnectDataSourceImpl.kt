package com.example.data.source

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
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

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    override suspend fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    override suspend fun getSteps(from: LocalDate, to: LocalDate): List<StepRecord> {
        val zone = ZoneId.systemDefault()
        val start = from.atStartOfDay(zone).toInstant()
        val end = to.plusDays(1).atStartOfDay(zone).toInstant()

        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
        )

        // Aggregate by local date
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
