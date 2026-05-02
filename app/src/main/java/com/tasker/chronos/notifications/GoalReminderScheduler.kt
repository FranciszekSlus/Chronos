// Plik: notifications/GoalReminderScheduler.kt
package com.tasker.chronos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tasker.chronos.data.models.Goal
import java.time.LocalDate
import java.util.*

object GoalReminderScheduler {

    /**
     * Zaplanuj powiadomienia dla celu:
     * - 3 dni przed deadline
     * - 1 dzień przed deadline
     */
    fun schedule(context: Context, goal: Goal) {
        if (goal.endDate == null) {
            android.util.Log.d("GoalReminder", "⚠️ Brak daty końcowej dla: ${goal.title}")
            return
        }

        try {
            val endDate = LocalDate.parse(goal.endDate)
            val progress = goal.getProgressPercentage() // ✅ Używamy Int (0-100)

            // Powiadomienie 3 dni przed
            scheduleDeadlineNotification(
                context = context,
                goal = goal,
                endDate = endDate,
                daysBefore = 3,
                progress = progress,
                notificationId = "${goal.id}_3days"
            )

            // Powiadomienie 1 dzień przed
            scheduleDeadlineNotification(
                context = context,
                goal = goal,
                endDate = endDate,
                daysBefore = 1,
                progress = progress,
                notificationId = "${goal.id}_1day"
            )

            android.util.Log.d("GoalReminder", "✅ Zaplanowano przypomnienia dla: ${goal.title}")
        } catch (e: Exception) {
            android.util.Log.e("GoalReminder", "❌ Błąd planowania: ${e.message}", e)
        }
    }

    private fun scheduleDeadlineNotification(
        context: Context,
        goal: Goal,
        endDate: LocalDate,
        daysBefore: Int,
        progress: Int,
        notificationId: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, GoalReminderReceiver::class.java).apply {
            putExtra("goal_id", goal.id)
            putExtra("goal_title", goal.title)
            putExtra("days_before", daysBefore)
            putExtra("end_date", goal.endDate)
            putExtra("progress", progress) // ✅ Dodaj progress
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Oblicz datę powiadomienia
        val notificationDate = endDate.minusDays(daysBefore.toLong())
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, notificationDate.year)
            set(Calendar.MONTH, notificationDate.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, notificationDate.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 9) // Powiadomienie o 9:00
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0) // ✅ Wyzeruj milisekundy
        }

        // Nie planuj jeśli data już minęła
        if (calendar.timeInMillis > System.currentTimeMillis()) {
            AlarmSchedulerCompat.scheduleWakeupAlarm(
                alarmManager = alarmManager,
                triggerAtMillis = calendar.timeInMillis,
                pendingIntent = pendingIntent
            )

            android.util.Log.d(
                "GoalReminder",
                "✅ Zaplanowano $daysBefore-dniowe przypomnienie: ${goal.title} na ${calendar.time}"
            )
        } else {
            android.util.Log.d(
                "GoalReminder",
                "⏰ Czas minął, pomijam $daysBefore-dniowe przypomnienie dla: ${goal.title}"
            )
        }
    }

    fun cancel(context: Context, goal: Goal) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Anuluj oba powiadomienia (3 dni i 1 dzień)
            listOf("${goal.id}_3days", "${goal.id}_1day").forEach { notificationId ->
                val intent = Intent(context, GoalReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel() // ✅ Anuluj też PendingIntent
            }

            android.util.Log.d("GoalReminder", "🗑️ Anulowano przypomnienia dla: ${goal.title}")
        } catch (e: Exception) {
            android.util.Log.e("GoalReminder", "❌ Błąd anulowania: ${e.message}", e)
        }
    }

    fun reschedule(context: Context, goal: Goal) {
        cancel(context, goal)
        schedule(context, goal)
    }
    /**
     * ✅ NOWE: Zaplanuj cykliczne przypomnienia co X tygodni
     */
    fun schedulePeriodicReminder(context: Context, goal: Goal) {
        if (!goal.hasPeriodicReminder) return

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val today = LocalDate.now()

            // Znajdź następne wystąpienie (za reminderIntervalWeeks tygodni od dziś)
            val nextDate = today.plusWeeks(goal.reminderIntervalWeeks.toLong())

            // Nie planuj jeśli przekroczymy deadline
            if (goal.endDate != null) {
                val endDate = LocalDate.parse(goal.endDate)
                if (nextDate.isAfter(endDate)) {
                    android.util.Log.d("GoalReminder", "⏭️ Następne przypomnienie po deadline - pomijam")
                    return
                }
            }

            val timeParts = goal.reminderTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, nextDate.year)
                set(Calendar.MONTH, nextDate.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, nextDate.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val intent = Intent(context, GoalReminderReceiver::class.java).apply {
                putExtra("goal_id", goal.id)
                putExtra("goal_title", goal.title)
                putExtra("days_before", -1)  // -1 = cykliczne (nie deadline)
                putExtra("end_date", goal.endDate ?: "")
                putExtra("progress", goal.getProgressPercentage())
                putExtra("is_periodic", true)
                putExtra("interval_weeks", goal.reminderIntervalWeeks)
                putExtra("reminder_time", goal.reminderTime)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                "${goal.id}_periodic".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (calendar.timeInMillis > System.currentTimeMillis()) {
                AlarmSchedulerCompat.scheduleWakeupAlarm(
                    alarmManager = alarmManager,
                    triggerAtMillis = calendar.timeInMillis,
                    pendingIntent = pendingIntent
                )
                android.util.Log.d("GoalReminder", "✅ Cykliczne przypomnienie: ${goal.title} za ${goal.reminderIntervalWeeks} tyg ($nextDate)")
            }
        } catch (e: Exception) {
            android.util.Log.e("GoalReminder", "❌ Błąd cyklicznego planowania: ${e.message}")
        }
    }

    fun cancelPeriodicReminder(context: Context, goal: Goal) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, GoalReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${goal.id}_periodic".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}