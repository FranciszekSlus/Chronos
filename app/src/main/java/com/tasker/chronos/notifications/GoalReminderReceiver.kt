// Plik: notifications/GoalReminderReceiver.kt
package com.tasker.chronos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tasker.chronos.MainActivity
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

        createNotificationChannel(context)
        sendNotification(context, goalId, goalTitle, daysBefore, progress)
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
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
}