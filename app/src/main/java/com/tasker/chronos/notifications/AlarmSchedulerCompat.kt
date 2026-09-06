package com.tasker.chronos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build

/**
 * Schedules wakeup alarms with exact-alarm support and inexact fallback.
 */
object AlarmSchedulerCompat {

    fun scheduleWakeupAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) {
            android.util.Log.w("AlarmSchedulerCompat", "Trigger in the past — skip")
            return
        }

        try {
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            when {
                canExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
                else -> {
                    @Suppress("DEPRECATION")
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.w(
                "AlarmSchedulerCompat",
                "Exact alarm denied — falling back to inexact: ${e.message}"
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (fallback: Exception) {
                android.util.Log.e(
                    "AlarmSchedulerCompat",
                    "Failed to schedule alarm: ${fallback.message}",
                    fallback
                )
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "AlarmSchedulerCompat",
                "Failed to schedule alarm: ${e.message}",
                e
            )
        }
    }
}
