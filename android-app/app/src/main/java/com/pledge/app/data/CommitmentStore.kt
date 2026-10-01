package com.pledge.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistence manager for BozPledge Escrow state and Wallet sessions.
 */
object CommitmentStore {
    private const val PREFS_NAME = "bozpledge_storage"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveCommitment(context: Context, state: CommitmentState) {
        val editor = getPrefs(context).edit()
        editor.putLong("commitmentId", state.commitmentId)
        editor.putString("authority", state.authority)
        editor.putString("clockInAuthority", state.clockInAuthority)
        editor.putInt("targetSteps", state.targetSteps)
        editor.putInt("totalDays", state.totalDays)
        editor.putInt("completedDays", state.completedDays)
        editor.putLong("dayDurationSec", state.dayDurationSec)
        editor.putLong("startTimestamp", state.startTimestamp)
        editor.putString("totalAmountSKR", state.totalAmountSKR.toString())
        editor.putBoolean("settled", state.settled)
        editor.putLong("clockedInBitmap", state.clockedInBitmap)

        // Rule fields
        editor.putString("rule_id", state.rule.id)
        editor.putString("rule_title", state.rule.title)
        editor.putString("rule_sensor_type", state.rule.sensorType.name)
        editor.putString("rule_schedule", state.rule.schedulePreset.name)
        editor.putString("rule_limit", state.rule.thresholdLimit.toString())
        editor.putString("rule_unit", state.rule.unit)
        editor.putBoolean("rule_is_ceiling", state.rule.isLimitCeiling)

        editor.apply()
    }

    fun loadCommitment(context: Context): CommitmentState? {
        val prefs = getPrefs(context)
        if (!prefs.contains("startTimestamp")) {
            return null
        }

        val startTimestamp = prefs.getLong("startTimestamp", 0L)
        val commitmentId = prefs.getLong("commitmentId", 0L)
        val authority = prefs.getString("authority", "") ?: ""
        val clockInAuthority = prefs.getString("clockInAuthority", "") ?: ""
        val targetSteps = prefs.getInt("targetSteps", 8000)
        val totalDays = prefs.getInt("totalDays", 7)
        val completedDays = prefs.getInt("completedDays", 0)
        val dayDurationSec = prefs.getLong("dayDurationSec", 86400L)
        val totalAmountSKR = prefs.getString("totalAmountSKR", "2500.0")?.toDoubleOrNull() ?: 2500.0
        val settled = prefs.getBoolean("settled", false)
        val clockedInBitmap = prefs.getLong("clockedInBitmap", 0L)

        val ruleId = prefs.getString("rule_id", "odd_internet") ?: "odd_internet"
        val ruleTitle = prefs.getString("rule_title", "Odd Days Internet Detox") ?: "Odd Days Internet Detox"
        val sensorTypeName = prefs.getString("rule_sensor_type", SensorType.NETWORK_DATA.name)
        val sensorType = try { SensorType.valueOf(sensorTypeName ?: SensorType.NETWORK_DATA.name) } catch (e: Exception) { SensorType.NETWORK_DATA }
        val scheduleName = prefs.getString("rule_schedule", SchedulePreset.ODD_DAYS.name)
        val schedule = try { SchedulePreset.valueOf(scheduleName ?: SchedulePreset.ODD_DAYS.name) } catch (e: Exception) { SchedulePreset.ODD_DAYS }
        val thresholdLimit = prefs.getString("rule_limit", "60.0")?.toDoubleOrNull() ?: 60.0
        val unit = prefs.getString("rule_unit", "mins") ?: "mins"
        val isLimitCeiling = prefs.getBoolean("rule_is_ceiling", true)

        val rule = HabitRule(
            id = ruleId,
            title = ruleTitle,
            sensorType = sensorType,
            schedulePreset = schedule,
            activeDaysOfWeek = if (schedule == SchedulePreset.ODD_DAYS) setOf(1, 3, 5, 7) else (1..7).toSet(),
            thresholdLimit = thresholdLimit,
            unit = unit,
            isLimitCeiling = isLimitCeiling
        )

        return CommitmentState(
            commitmentId = commitmentId,
            authority = authority,
            clockInAuthority = clockInAuthority,
            targetSteps = targetSteps,
            totalDays = totalDays,
            completedDays = completedDays,
            dayDurationSec = dayDurationSec,
            startTimestamp = startTimestamp,
            totalAmountSKR = totalAmountSKR,
            settled = settled,
            clockedInBitmap = clockedInBitmap,
            rule = rule
        )
    }

    fun clearCommitment(context: Context) {
        val editor = getPrefs(context).edit()
        editor.remove("startTimestamp")
        editor.remove("commitmentId")
        editor.remove("completedDays")
        editor.remove("clockedInBitmap")
        editor.remove("settled")
        editor.apply()
    }
}
