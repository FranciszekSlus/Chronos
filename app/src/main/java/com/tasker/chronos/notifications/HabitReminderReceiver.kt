// Plik: notifications/HabitReminderReceiver.kt
package com.tasker.chronos.notifications

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
import com.tasker.chronos.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class HabitReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "chronos_habits"
    }

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("HabitReminder", "")
        android.util.Log.d("HabitReminder", "🔔🔔🔔 RECEIVER WYWOŁANY! 🔔🔔🔔")
        android.util.Log.d("HabitReminder", "Czas: ${java.time.LocalDateTime.now()}")

        val habitId = intent.getStringExtra("habit_id")
        val habitName = intent.getStringExtra("habit_name")

        android.util.Log.d("HabitReminder", "habitId: $habitId")
        android.util.Log.d("HabitReminder", "habitName: $habitName")

        if (habitId == null || habitName == null) {
            android.util.Log.e("HabitReminder", "❌ Brak habitId lub habitName - KONIEC")
            return
        }

        // ✅ Pobierz dane o typie nawyku
        val isWeekly = intent.getBooleanExtra("is_weekly", false)
        val isMonthly = intent.getBooleanExtra("is_monthly", false)
        val weeklyDays = intent.getStringArrayExtra("weekly_days")?.toList() ?: emptyList()
        val monthlyDates = intent.getStringArrayExtra("monthly_dates")?.toList() ?: emptyList()

        android.util.Log.d("HabitReminder", "isWeekly: $isWeekly")
        android.util.Log.d("HabitReminder", "isMonthly: $isMonthly")
        android.util.Log.d("HabitReminder", "weeklyDays: $weeklyDays")
        android.util.Log.d("HabitReminder", "monthlyDates: $monthlyDates")

        // ✅ KLUCZOWA WALIDACJA: Sprawdź czy nawyk jest aktywny dzisiaj
        val today = LocalDate.now()
        android.util.Log.d("HabitReminder", "Dzisiaj: $today (${today.dayOfWeek})")

        val isActiveToday = when {
            isWeekly -> {
                val todayDayName = today.dayOfWeek.name
                val active = weeklyDays.contains(todayDayName)
                android.util.Log.d("HabitReminder", "WALIDACJA TYGODNIOWA:")
                android.util.Log.d("HabitReminder", "  Dziś: $todayDayName")
                android.util.Log.d("HabitReminder", "  Aktywne dni: $weeklyDays")
                android.util.Log.d("HabitReminder", "  Czy zawiera? $active")
                active
            }
            isMonthly -> {
                val todayDayOfMonth = today.dayOfMonth.toString()
                val active = monthlyDates.contains(todayDayOfMonth)
                android.util.Log.d("HabitReminder", "WALIDACJA MIESIĘCZNA:")
                android.util.Log.d("HabitReminder", "  Dziś: $todayDayOfMonth")
                android.util.Log.d("HabitReminder", "  Aktywne dni: $monthlyDates")
                android.util.Log.d("HabitReminder", "  Czy zawiera? $active")
                active
            }
            else -> {
                android.util.Log.d("HabitReminder", "CODZIENNY NAWYK - zawsze aktywny")
                true
            }
        }

        android.util.Log.d("HabitReminder", "WYNIK WALIDACJI: isActiveToday = $isActiveToday")

        if (!isActiveToday) {
            android.util.Log.d("HabitReminder", "⏭️ Nawyk nieaktywny dzisiaj - POMIJAM")

            if (isMonthly) {
                android.util.Log.d("HabitReminder", "📅 Miesięczny - planuję następny miesiąc")
                rescheduleMonthlyReminder(context, intent)
            }

            android.util.Log.d("HabitReminder", "═══════════════════════════════════")
            return
        }

        // ✅ Wyślij powiadomienie
        android.util.Log.d("HabitReminder", "✅ WYSYŁAM POWIADOMIENIE!")
        createNotificationChannel(context)
        sendNotification(context, habitId, habitName)

        // ✅ KLUCZOWE: Zaplanuj ponownie następne powiadomienie
        android.util.Log.d("HabitReminder", "🔄 PLANUJĘ NASTĘPNE POWIADOMIENIE...")
        rescheduleNextReminder(context, intent)

        android.util.Log.d("HabitReminder", "═══════════════════════════════════")
    }

    /**
     * ✅ NOWE: Zaplanuj następne powiadomienie dla nawyku
     */
    private fun rescheduleNextReminder(context: Context, originalIntent: Intent) {
        try {
            val habitId = originalIntent.getStringExtra("habit_id") ?: return
            val habitName = originalIntent.getStringExtra("habit_name") ?: return
            val reminderTime = originalIntent.getStringExtra("reminder_time") ?: "09:00"
            val isWeekly = originalIntent.getBooleanExtra("is_weekly", false)
            val isMonthly = originalIntent.getBooleanExtra("is_monthly", false)
            val weeklyDays = originalIntent.getStringArrayExtra("weekly_days")?.toList() ?: emptyList()
            val monthlyDates = originalIntent.getStringArrayExtra("monthly_dates")?.toList() ?: emptyList()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val time = LocalTime.parse(reminderTime)
            val today = LocalDate.now()

            when {
                // CODZIENNY - jutro o tej samej godzinie
                !isWeekly && !isMonthly -> {
                    val tomorrow = today.plusDays(1)
                    val nextDateTime = LocalDateTime.of(tomorrow, time)
                    val triggerTime = nextDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                    // POPRAWKA - codzienny:
                    // POPRAWKA - codzienny:
                    val extras = originalIntent.extras ?: return
                    val newIntent = Intent(context, HabitReminderReceiver::class.java).apply {
                        putExtras(extras)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        habitId.hashCode(),
                        newIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    AlarmSchedulerCompat.scheduleWakeupAlarm(
                        alarmManager = alarmManager,
                        triggerAtMillis = triggerTime,
                        pendingIntent = pendingIntent
                    )

                    android.util.Log.d("HabitReminder", "   📅 Codzienny - jutro: $tomorrow")
                }

                // TYGODNIOWY - następny wybrany dzień
                isWeekly -> {
                    val targetDay = originalIntent.getStringExtra("target_day") ?: return

                    val dayOfWeek = java.time.DayOfWeek.valueOf(targetDay)
                    // Znajdź następne wystąpienie tego dnia tygodnia (za 7 dni od dzisiaj)
                    val nextDate = today.plusWeeks(1).with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek))
                    val nextDateTime = LocalDateTime.of(nextDate, time)
                    val triggerTime = nextDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                    // POPRAWKA - tygodniowy:
                    // POPRAWKA - tygodniowy:
                    val extras = originalIntent.extras ?: return
                    val newIntent = Intent(context, HabitReminderReceiver::class.java).apply {
                        putExtras(extras)
                    }

                    val uniqueId = "${habitId}_$targetDay".hashCode()
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        uniqueId,
                        newIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    AlarmSchedulerCompat.scheduleWakeupAlarm(
                        alarmManager = alarmManager,
                        triggerAtMillis = triggerTime,
                        pendingIntent = pendingIntent
                    )


                }

                // MIESIĘCZNY - obsłużone w rescheduleMonthlyReminder
                isMonthly -> {
                    rescheduleMonthlyReminder(context, originalIntent)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitReminder", "❌ Błąd reschedule: ${e.message}", e)
        }
    }

    /**
     * Zaplanuj ponownie przypomnienie miesięczne na następny miesiąc
     */
    private fun rescheduleMonthlyReminder(context: Context, originalIntent: Intent) {
        try {
            val habitId = originalIntent.getStringExtra("habit_id") ?: return
            val targetDayOfMonth = originalIntent.getIntExtra("target_day_of_month", 0)
            if (targetDayOfMonth == 0) return

            val today = LocalDate.now()
            val nextMonth = today.plusMonths(1)

            // Uwzględnij różną liczbę dni w miesiącach (np. 31 → 30)
            val nextDate = nextMonth.withDayOfMonth(
                targetDayOfMonth.coerceAtMost(nextMonth.lengthOfMonth())
            )

            // Użyj tej samej godziny co oryginalne powiadomienie
            val reminderTime = originalIntent.getStringExtra("reminder_time") ?: "09:00"
            val time = try { LocalTime.parse(reminderTime) } catch (e: Exception) { LocalTime.of(9, 0) }
            val nextDateTime = LocalDateTime.of(nextDate, time)
            val triggerTime = nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val uniqueId = "${habitId}_day$targetDayOfMonth".hashCode()

            // POPRAWKA - miesięczny (rescheduleMonthlyReminder):
            // POPRAWKA - miesięczny (rescheduleMonthlyReminder):
            val extras = originalIntent.extras ?: return
            val newIntent = Intent(context, HabitReminderReceiver::class.java).apply {
                putExtras(extras)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueId,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            AlarmSchedulerCompat.scheduleWakeupAlarm(
                alarmManager = alarmManager,
                triggerAtMillis = triggerTime,
                pendingIntent = pendingIntent
            )

            android.util.Log.d(
                "HabitReminder",
                "🔄 Zaplanowano na następny miesiąc: dzień $targetDayOfMonth ($nextDate)"
            )
        } catch (e: Exception) {
            android.util.Log.e("HabitReminder", "❌ Błąd planowania: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Przypomnienia o nawykach",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Powiadomienia o nawykach Chronos"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(context: Context, habitId: String, habitName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_habit", habitId)
            putExtra("item_type", "habit")
            putExtra("item_id", habitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Czas na nawyk! 🎯")
            .setContentText(habitName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(habitId.hashCode(), notification)

        android.util.Log.d("HabitReminder", "✅ Wysłano powiadomienie: $habitName")
    }
}