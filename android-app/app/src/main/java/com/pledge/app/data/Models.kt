package com.pledge.app.data

enum class SensorType(val displayName: String, val apiSource: String, val defaultUnit: String) {
    NETWORK_DATA("Internet & Data Usage", "NetworkStatsManager", "mins"),
    SCREEN_DETOX("Screen Time Limit", "UsageStatsManager", "hours"),
    HEALTH_STEPS("Physical Step Count", "Health Connect", "steps"),
    WAKE_UP_CLOCK("Early Morning Wake-Up", "System NTP Hardware Clock", "AM")
}

enum class SchedulePreset(val displayName: String) {
    ODD_DAYS("Odd Days (Mon, Wed, Fri, Sun)"),
    EVEN_DAYS("Even Days (Tue, Thu, Sat)"),
    DAILY("Every Day"),
    CUSTOM("Custom Days")
}

data class HabitRule(
    val id: String = "odd_internet",
    val title: String = "Odd Days Internet Detox",
    val sensorType: SensorType = SensorType.NETWORK_DATA,
    val schedulePreset: SchedulePreset = SchedulePreset.ODD_DAYS,
    val activeDaysOfWeek: Set<Int> = setOf(1, 3, 5, 7), // 1=Mon, 3=Wed, 5=Fri, 7=Sun
    val thresholdLimit: Double = 60.0, // 60 mins max
    val unit: String = "mins",
    val isLimitCeiling: Boolean = true // true: less is passing (e.g. internet <= 60m), false: more is passing (steps >= 10,000)
)

data class CommitmentState(
    val commitmentId: Long = 0L,
    val authority: String = "",
    val clockInAuthority: String = "",
    val targetSteps: Int = 8000,
    val totalDays: Int = 7,
    val completedDays: Int = 0,
    val dayDurationSec: Long = 86400L,
    val startTimestamp: Long = 0L,
    val totalAmountSKR: Double = 2500.0,
    val settled: Boolean = false,
    val clockedInBitmap: Long = 0L,
    val rule: HabitRule = HabitRule()
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

    val dailyBurnAtRiskSKR: Double
        get() = if (totalDays > 0) totalAmountSKR / totalDays else 0.0
}

data class DemoConfig(
    val isDemoMode: Boolean = true,
    val manualStepCount: Int = 8500,
    val manualInternetMins: Int = 38,
    val demoDayDurationSec: Long = 30L
)

