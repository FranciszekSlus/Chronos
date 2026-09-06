// Plik: notifications/TaskReminderScheduler.kt
package com.tasker.chronos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tasker.chronos.data.models.Task
import com.tasker.chronos.data.models.ReminderType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object TaskReminderScheduler {

    /**
     * Zaplanuj przypomnienie dla zadania
     */
    fun schedule(context: Context, task: Task) {
        if (!task.hasReminder || task.date == null) {
            android.util.Log.d("TaskReminderScheduler", "⚠️ Brak przypomnienia lub daty dla: ${task.title}")
            return
        }

        val triggerTime = calculateTriggerTime(task) ?: run {
            android.util.Log.e("TaskReminderScheduler", "❌ Nie udało się obliczyć czasu przypomnienia")
            return
        }

        val now = System.currentTimeMillis()
        // Jeśli wyliczony czas już minął, ale termin zadania jest w przyszłości — powiadom za ~15s
        val taskEndMillis = try {
            val d = LocalDate.parse(task.date)
            val t = task.time?.let { LocalTime.parse(it) } ?: LocalTime.of(23, 59)
            LocalDateTime.of(d, t).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }

        val effectiveTrigger = when {
            triggerTime > now -> triggerTime
            taskEndMillis != null && taskEndMillis > now -> now + 15_000L
            else -> {
                android.util.Log.w("TaskReminderScheduler", "⚠️ Przypomnienie w przeszłości: ${task.title}")
                return
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_description", task.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        AlarmSchedulerCompat.scheduleWakeupAlarm(
            alarmManager = alarmManager,
            triggerAtMillis = effectiveTrigger,
            pendingIntent = pendingIntent
        )

        val triggerDateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(effectiveTrigger),
            ZoneId.systemDefault()
        )

        android.util.Log.d("TaskReminderScheduler", "✅ Zaplanowano przypomnienie: ${task.title}")
        android.util.Log.d("TaskReminderScheduler", "   Typ: ${task.reminderType}")
        android.util.Log.d("TaskReminderScheduler", "   Czas: $triggerDateTime")
    }

    /**
     * ✅ NAPRAWIONE: reminderType ma wartość domyślną
     */
    private fun calculateTriggerTime(task: Task): Long? {
        return try {
            val taskDate = LocalDate.parse(task.date)
            val taskTime = task.time?.let { LocalTime.parse(it) } ?: LocalTime.of(9, 0)
            val taskDateTime = LocalDateTime.of(taskDate, taskTime)

            // ✅ Bezpośrednie użycie task.reminderType
            val triggerDateTime = when (task.reminderType) {
                ReminderType.MINUTES_BEFORE -> {
                    taskDateTime.minusMinutes(task.reminderMinutesBefore.toLong())
                }

                ReminderType.CUSTOM_TIME -> {
                    val customTime = task.reminderCustomTime?.let { LocalTime.parse(it) }
                        ?: LocalTime.of(9, 0)
                    LocalDateTime.of(taskDate, customTime)
                }

                ReminderType.DAYS_BEFORE -> {
                    val daysBeforeDate = taskDate.minusDays(task.reminderCustomDays?.toLong() ?: 1)
                    val customTime = task.reminderCustomTime?.let { LocalTime.parse(it) }
                        ?: LocalTime.of(9, 0)
                    LocalDateTime.of(daysBeforeDate, customTime)
                }
            }

            triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            android.util.Log.e("TaskReminderScheduler", "❌ Błąd obliczania czasu: ${e.message}", e)
            null
        }
    }

    /**
     * Zaktualizuj przypomnienie (anuluj stare i zaplanuj nowe)
     */
    fun reschedule(context: Context, task: Task) {
        cancel(context, task)
        schedule(context, task)
    }

    /**
     * Anuluj przypomnienie
     */
    fun cancel(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        android.util.Log.d("TaskReminderScheduler", "🗑️ Anulowano przypomnienie: ${task.title}")
    }
}