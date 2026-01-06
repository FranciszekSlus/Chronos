// Plik: utils/CustomEventNotificationReceiver.kt
package com.tasker.chronos.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tasker.chronos.MainActivity

/**
 * Receiver dla powiadomień custom events
 */
class CustomEventNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "chronos_custom_events"
        const val CHANNEL_NAME = "Wydarzenia kalendarzowe"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra("event_id") ?: return
        val title = intent.getStringExtra("title") ?: "Przypomnienie"
        val description = intent.getStringExtra("description") ?: ""
        val startTime = intent.getStringExtra("start_time") ?: ""
        val notificationType = intent.getStringExtra("notification_type") ?: "START"
        val minutesBefore = intent.getIntExtra("minutes_before", 0)

        android.util.Log.d("CustomEventReceiver", "🔔 Powiadomienie ($notificationType): $title")

        showNotification(
            context,
            eventId.hashCode(),
            title,
            description,
            startTime,
            notificationType,
            minutesBefore
        )
    }

    private fun showNotification(
        context: Context,
        id: Int,
        title: String,
        description: String,
        startTime: String,
        notificationType: String,
        minutesBefore: Int
    ) {
        ensureNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Różne treści w zależności od typu powiadomienia
        val message = when (notificationType) {
            "REMINDER" -> buildString {
                append("Za ${formatMinutes(minutesBefore)}")
                append("\nRozpoczęcie: $startTime")
                if (description.isNotEmpty()) {
                    append("\n$description")
                }
            }
            "START" -> buildString {
                append("Wydarzenie zaczyna się teraz!")
                if (description.isNotEmpty()) {
                    append("\n$description")
                }
            }
            else -> description
        }

        val notificationTitle = when (notificationType) {
            "REMINDER" -> "⏰ Przypomnienie: $title"
            "START" -> "📅 Rozpoczęcie: $title"
            else -> "📅 $title"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationTitle)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)

        android.util.Log.d("CustomEventReceiver", "✅ Wyświetlono powiadomienie: $notificationTitle")
    }

    /**
     * Formatuj minuty na czytelny tekst
     */
    private fun formatMinutes(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes minut"
            minutes < 1440 -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                if (remainingMinutes == 0) {
                    "$hours ${if (hours == 1) "godzinę" else if (hours < 5) "godziny" else "godzin"}"
                } else {
                    "$hours godz. $remainingMinutes min"
                }
            }
            else -> {
                val days = minutes / 1440
                "$days ${if (days == 1) "dzień" else "dni"}"
            }
        }
    }

    // ✅ DODAJ FUNKCJĘ TWORZENIA KANAŁU
    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Przypomnienia o wydarzeniach z kalendarza"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
