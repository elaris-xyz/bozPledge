package com.pledge.app.data

data class CommitmentState(
    val commitmentId: Long = 0L,
    val authority: String = "",
    val clockInAuthority: String = "",
    val targetSteps: Int = 8000,
    val totalDays: Int = 7,
    val completedDays: Int = 0,
    val dayDurationSec: Long = 86400L,
    val startTimestamp: Long = 0L,
    val totalAmountSKR: Double = 500.0,
    val settled: Boolean = false,
    val clockedInBitmap: Long = 0L
) {
    val isActive: Boolean
        get() = startTimestamp > 0L && !settled && currentDayIndex < totalDays

    val currentDayIndex: Int
        get() {
            if (startTimestamp == 0L || dayDurationSec <= 0L) return 0
            val nowSec = System.currentTimeMillis() / 1000L
            val elapsed = nowSec - startTimestamp
            if (elapsed < 0) return 0
            val day = (elapsed / dayDurationSec).toInt()
            return if (day >= totalDays) totalDays else day
        }

    val isTodayClockedIn: Boolean
        get() {
            if (currentDayIndex >= totalDays) return false
            val mask = 1L shl currentDayIndex
            return (clockedInBitmap and mask) != 0L
        }

    val secondsLeftInCurrentDay: Long
        get() {
            if (startTimestamp == 0L || dayDurationSec <= 0L) return 0L
            val nowSec = System.currentTimeMillis() / 1000L
            val elapsed = nowSec - startTimestamp
            val currentDayEnd = startTimestamp + ((currentDayIndex + 1) * dayDurationSec)
            val left = currentDayEnd - nowSec
            return if (left > 0) left else 0L
        }

    val completionRate: Float
        get() = if (totalDays > 0) completedDays.toFloat() / totalDays.toFloat() else 0f

    val refundAmountSKR: Double
        get() = if (totalDays > 0) (totalAmountSKR * completedDays) / totalDays else 0.0

    val burnAmountSKR: Double
        get() = totalAmountSKR - refundAmountSKR
}

data class DemoConfig(
    val isDemoMode: Boolean = true,
    val manualStepCount: Int = 8500,
    val demoDayDurationSec: Long = 30L
)
