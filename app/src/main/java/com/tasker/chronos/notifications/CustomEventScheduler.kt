// Plik: utils/CustomEventScheduler.kt
package com.tasker.chronos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tasker.chronos.data.models.CustomEvent
import com.tasker.chronos.utils.CustomEventNotificationReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object CustomEventScheduler {

    /**
     * Zaplanuj powiadomienia dla wydarzenia
     */
    fun schedule(context: Context, event: CustomEvent) {
        android.util.Log.d("CustomEventScheduler", "📅 Planowanie powiadomień dla: ${event.title}")

        // 1. ✅ ZAWSZE zaplanuj powiadomienie w momencie rozpoczęcia
        scheduleStartNotification(context, event)

        // 2. ✅ Jeśli ma dodatkowe przypomnienie, zaplanuj je
        if (event.hasReminder && event.reminderMinutesBefore > 0) {
            scheduleReminderNotification(context, event)
        }
    }

    /**
     * ✅ Powiadomienie w momencie rozpoczęcia wydarzenia
     */
    private fun scheduleStartNotification(context: Context, event: CustomEvent) {
        try {
            val startDateTime = LocalDateTime.of(
                LocalDate.parse(event.date),
                LocalTime.parse(event.startTime)
            )

            val triggerTimeMillis = startDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            // Sprawdź czy wydarzenie nie jest w przeszłości
            if (triggerTimeMillis <= System.currentTimeMillis()) {
                android.util.Log.w("CustomEventScheduler", "⚠️ Wydarzenie w przeszłości, pomijam powiadomienie rozpoczęcia")
                return
            }

            val intent = Intent(context, CustomEventNotificationReceiver::class.java).apply {
                putExtra("event_id", event.id)
                putExtra("event_date", event.date)
                putExtra("title", event.title)
                putExtra("description", event.description)
                putExtra("start_time", event.startTime)
                putExtra("notification_type", "START")  // ✅ Typ powiadomienia
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                event.id.hashCode(),  // Unikalny ID dla powiadomienia rozpoczęcia
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            AlarmSchedulerCompat.scheduleWakeupAlarm(
                alarmManager = alarmManager,
                triggerAtMillis = triggerTimeMillis,
                pendingIntent = pendingIntent
            )

            android.util.Log.d(
                "CustomEventScheduler",
                "✅ Zaplanowano powiadomienie START: ${event.title} na $startDateTime"
            )
        } catch (e: Exception) {
            android.util.Log.e("CustomEventScheduler", "❌ Błąd planowania START: ${e.message}")
        }
    }

    /**
     * ✅ Dodatkowe przypomnienie przed wydarzeniem
     */
    private fun scheduleReminderNotification(context: Context, event: CustomEvent) {
        try {
            val startDateTime = LocalDateTime.of(
                LocalDate.parse(event.date),
                LocalTime.parse(event.startTime)
            )

            // Oblicz czas powiadomienia (X minut przed)
            val reminderDateTime = startDateTime.minusMinutes(event.reminderMinutesBefore.toLong())

            val triggerTimeMillis = reminderDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            // Sprawdź czy przypomnienie nie jest w przeszłości
            if (triggerTimeMillis <= System.currentTimeMillis()) {
                android.util.Log.w("CustomEventScheduler", "⚠️ Przypomnienie w przeszłości, pomijam")
                return
            }

            val intent = Intent(context, CustomEventNotificationReceiver::class.java).apply {
                putExtra("event_id", "${event.id}_reminder")  // ✅ Inne ID dla przypomnienia
                putExtra("event_date", event.date)
                putExtra("title", event.title)
                putExtra("description", event.description)
                putExtra("start_time", event.startTime)
                putExtra("notification_type", "REMINDER")  // ✅ Typ powiadomienia
                putExtra("minutes_before", event.reminderMinutesBefore)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                "${event.id}_reminder".hashCode(),  // Unikalny ID dla przypomnienia
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            AlarmSchedulerCompat.scheduleWakeupAlarm(
                alarmManager = alarmManager,
                triggerAtMillis = triggerTimeMillis,
                pendingIntent = pendingIntent
            )

            android.util.Log.d(
                "CustomEventScheduler",
                "✅ Zaplanowano PRZYPOMNIENIE: ${event.title} na $reminderDateTime (${event.reminderMinutesBefore} min przed)"
            )
        } catch (e: Exception) {
            android.util.Log.e("CustomEventScheduler", "❌ Błąd planowania REMINDER: ${e.message}")
        }
    }

    /**
     * Anuluj wszystkie powiadomienia dla wydarzenia
     */
    fun cancel(context: Context, event: CustomEvent) {
        android.util.Log.d("CustomEventScheduler", "🔕 Anulowanie powiadomień dla: ${event.title}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Anuluj powiadomienie rozpoczęcia
        val startIntent = Intent(context, CustomEventNotificationReceiver::class.java)
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(startPendingIntent)

        // Anuluj przypomnienie (jeśli istnieje)
        if (event.hasReminder) {
            val reminderIntent = Intent(context, CustomEventNotificationReceiver::class.java)
            val reminderPendingIntent = PendingIntent.getBroadcast(
                context,
                "${event.id}_reminder".hashCode(),
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(reminderPendingIntent)
        }

        android.util.Log.d("CustomEventScheduler", "✅ Anulowano powiadomienia")
    }

    /**
     * Zaktualizuj powiadomienia (anuluj stare + zaplanuj nowe)
     */
    fun reschedule(context: Context, event: CustomEvent) {
        cancel(context, event)
        schedule(context, event)
    }
}