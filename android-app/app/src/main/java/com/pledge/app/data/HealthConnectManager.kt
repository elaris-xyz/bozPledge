package com.pledge.app.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    val healthConnectClient by lazy {
        if (isHealthConnectAvailable()) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readTodaySteps(isDemoMode: Boolean = false, manualDemoSteps: Int = 8500): Int {
        if (isDemoMode) {
            return manualDemoSteps
        }

        val client = healthConnectClient ?: return manualDemoSteps

        return try {
            val now = Instant.now()
            val startOfDay = now.truncatedTo(ChronoUnit.DAYS)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )

            val steps = response[StepsRecord.COUNT_TOTAL] ?: 0L
            steps.toInt()
        } catch (e: Exception) {
            // Fallback for emulator or ungranted permissions
            manualDemoSteps
        }
    }
}
