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
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
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
}