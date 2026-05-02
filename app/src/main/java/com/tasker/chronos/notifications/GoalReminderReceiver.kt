// Plik: notifications/GoalReminderReceiver.kt
package com.tasker.chronos.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tasker.chronos.MainActivity
import java.util.Calendar
import java.time.LocalDate
import com.tasker.chronos.R

class GoalReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "chronos_goals"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val goalId = intent.getStringExtra("goal_id") ?: return
        val goalTitle = intent.getStringExtra("goal_title") ?: "Cel"
        val daysBefore = intent.getIntExtra("days_before", 1)
        val progress = intent.getIntExtra("progress", 0)
        val isPeriodic = intent.getBooleanExtra("is_periodic", false)
        val intervalWeeks = intent.getIntExtra("interval_weeks", 4)
        val reminderTime = intent.getStringExtra("reminder_time") ?: "09:00"

        createNotificationChannel(context)
        sendNotification(context, goalId, goalTitle, daysBefore, progress)
        if (isPeriodic) {
            reschedulePeriodicReminder(context, goalId, goalTitle, intervalWeeks, reminderTime, intent.getStringExtra("end_date"))
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cele",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Przypomnienia o terminach celów"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ServiceCast")
    private fun reschedulePeriodicReminder(
        context: Context,
        goalId: String,
        goalTitle: String,
        intervalWeeks: Int,
        reminderTime: String,
        endDate: String?
    ) {
        try {
            val nextDate = LocalDate.now().plusWeeks(intervalWeeks.toLong())

            // Sprawdź deadline
            if (endDate != null && endDate.isNotBlank()) {
                if (nextDate.isAfter(LocalDate.parse(endDate))) {
                    android.util.Log.d("GoalReminder", "⏭️ Reschedule po deadline - koniec")
                    return
                }
            }

            val timeParts = reminderTime.split(":")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, nextDate.year)
                set(Calendar.MONTH, nextDate.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, nextDate.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val intent = Intent(context, GoalReminderReceiver::class.java).apply {
                putExtra("goal_id", goalId)
                putExtra("goal_title", goalTitle)
                putExtra("days_before", -1)
                putExtra("end_date", endDate ?: "")
                putExtra("progress", 0)
                putExtra("is_periodic", true)
                putExtra("interval_weeks", intervalWeeks)
                putExtra("reminder_time", reminderTime)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context, "${goalId}_periodic".hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            AlarmSchedulerCompat.scheduleWakeupAlarm(
                alarmManager = alarmManager,
                triggerAtMillis = calendar.timeInMillis,
                pendingIntent = pendingIntent
            )
            android.util.Log.d("GoalReminder", "🔄 Zaplanowano następne cykliczne: $nextDate")
        } catch (e: Exception) {
            android.util.Log.e("GoalReminder", "❌ Reschedule błąd: ${e.message}")
        }
    }

    private fun sendNotification(
        context: Context,
        goalId: String,
        goalTitle: String,
        daysBefore: Int,
        progress: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_goal", goalId)
            putExtra("item_type", "goal")
            putExtra("item_id", goalId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            "${goalId}_${daysBefore}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dostosuj treść w zależności od czasu i postępu
        val title = when (daysBefore) {
            3 -> "⏰ Cel kończy się za 3 dni!"
            1 -> "🚨 Cel kończy się jutro!"
            else -> "Przypomnienie o celu"
        }

        val message = buildString {
            append(goalTitle)
            if (progress > 0) {
                append("\nPostęp: $progress%")

                when {
                    progress >= 80 -> append(" - Prawie gotowe! 🎉")
                    progress >= 50 -> append(" - Dobra robota! 💪")
                    progress < 30 && daysBefore <= 1 -> append(" - Czas przyspieszyć! ⚡")
                }
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify("${goalId}_${daysBefore}".hashCode(), notification)

        android.util.Log.d("GoalReminder", "🔔 Notification sent: $goalTitle ($daysBefore days before)")
    }
    // ✅ Jeśli cykliczne — zaplanuj następne




}
