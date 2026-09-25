package com.pledge.app.data

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import java.time.LocalDate
import java.time.ZoneId

class NetworkStatsHelper(private val context: Context) {

    private val networkStatsManager by lazy {
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
    }

    /**
     * Reads today's data usage or screen time.
     * In demo mode or if permissions are not granted, returns simulated/manual value.
     */
    fun getTodayInternetMinutes(isDemoMode: Boolean = true, manualMins: Int = 38): Int {
        if (isDemoMode) {
            return manualMins
        }

        return try {
            val now = System.currentTimeMillis()
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val manager = networkStatsManager ?: return manualMins

            val bucket = manager.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI,
                null,
                startOfDay,
                now
            )
            val bytes = bucket.rxBytes + bucket.txBytes
            // Rough estimation of active minutes based on sustained throughput
            val estimatedMinutes = (bytes / (1024 * 1024 * 2)).toInt()
            if (estimatedMinutes > 0) estimatedMinutes else manualMins
        } catch (e: Exception) {
            manualMins
        }
    }

    /**
     * Checks if today's day of week matches an Odd Day schedule (Mon=1, Wed=3, Fri=5, Sun=7)
     */
    fun isTodayOddDay(): Boolean {
        val dayOfWeek = LocalDate.now().dayOfWeek.value
        return dayOfWeek in setOf(1, 3, 5, 7)
    }
}
