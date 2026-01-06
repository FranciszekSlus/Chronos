// Plik: notifications/HabitReminderScheduler.kt
package com.tasker.chronos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tasker.chronos.data.models.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

object HabitReminderScheduler {

    /**
     * ✅ INTELIGENTNE: Planuj przypomnienia tylko na dni, gdy nawyk jest aktywny
     */
    fun schedule(context: Context, habit: Habit) {
        android.util.Log.d("HabitReminder", "═══════════════════════════════════")
        android.util.Log.d("HabitReminder", "📅 PLANOWANIE dla: ${habit.name}")
        android.util.Log.d("HabitReminder", "   ID: ${habit.id}")
        android.util.Log.d("HabitReminder", "   hasReminder: ${habit.hasReminder}")
        android.util.Log.d("HabitReminder", "   reminderTime: ${habit.reminderTime}")
        android.util.Log.d("HabitReminder", "   weeklyDays: ${habit.weeklyDays}")
        android.util.Log.d("HabitReminder", "   monthlyDates: ${habit.monthlyDates}")

        if (!habit.hasReminder || habit.reminderTime.isNullOrBlank()) {
            android.util.Log.d("HabitReminder", "⚠️ Brak przypomnienia - KONIEC")
            android.util.Log.d("HabitReminder", "═══════════════════════════════════")
            return
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Sprawdź uprawnienia (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    android.util.Log.e("HabitReminder", "❌ Brak uprawnień do exact alarms!")
                    android.util.Log.d("HabitReminder", "═══════════════════════════════════")
                    return
                }
            }

            val time = LocalTime.parse(habit.reminderTime)
            android.util.Log.d("HabitReminder", "⏰ Godzina: $time")

            when {
                // ✅ CODZIENNY
                habit.weeklyDays.isEmpty() && habit.monthlyDates.isEmpty() -> {
                    android.util.Log.d("HabitReminder", "🔄 Wykryto: CODZIENNY")
                    scheduleDailyReminder(context, alarmManager, habit, time)
                }

                // ✅ TYGODNIOWY
                habit.weeklyDays.isNotEmpty() -> {
                    android.util.Log.d("HabitReminder", "📆 Wykryto: TYGODNIOWY")
                    scheduleWeeklyReminders(context, alarmManager, habit, time)
                }

                // ✅ MIESIĘCZNY
                habit.monthlyDates.isNotEmpty() -> {
                    android.util.Log.d("HabitReminder", "📅 Wykryto: MIESIĘCZNY")
                    scheduleMonthlyReminders(context, alarmManager, habit, time)
                }
            }

            android.util.Log.d("HabitReminder", "✅ ZAKOŃCZONO planowanie")
            android.util.Log.d("HabitReminder", "═══════════════════════════════════")
        } catch (e: Exception) {
            android.util.Log.e("HabitReminder", "❌ BŁĄD KRYTYCZNY: ${e.message}", e)
            android.util.Log.d("HabitReminder", "═══════════════════════════════════")
        }
    }

    /**
     * Codzienny nawyk - exact alarm z auto-reschedule w Receiver
     */
    private fun scheduleDailyReminder(
        context: Context,
        alarmManager: AlarmManager,
        habit: Habit,
        time: LocalTime
    ) {
        val intent = createIntent(context, habit)
        val pendingIntent = createPendingIntent(context, habit, intent)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // ✅ ZMIANA: Użyj setExactAndAllowWhileIdle zamiast setRepeating
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        android.util.Log.d("HabitReminder", "   Typ: Codzienny (exact alarm)")
        android.util.Log.d("HabitReminder", "   Następne: ${calendar.time}")
    }

    /**
     * Tygodniowy nawyk - osobne alarmy na każdy wybrany dzień
     */
    private fun scheduleWeeklyReminders(
        context: Context,
        alarmManager: AlarmManager,
        habit: Habit,
        time: LocalTime
    ) {
        val today = LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek

        android.util.Log.d("HabitReminder", "   Dziś: ${today.dayOfWeek} ($today)")
        android.util.Log.d("HabitReminder", "   Wybrane dni: ${habit.weeklyDays}")

        habit.weeklyDays.forEachIndexed { index, dayName ->
            android.util.Log.d("HabitReminder", "   ───────────────────")
            android.util.Log.d("HabitReminder", "   Przetwarzam: $dayName")

            try {
                val targetDay = DayOfWeek.valueOf(dayName)
                android.util.Log.d("HabitReminder", "   Target day enum: $targetDay (value: ${targetDay.value})")

                // Oblicz następne wystąpienie tego dnia
                var daysUntil = targetDay.value - currentDayOfWeek.value
                android.util.Log.d("HabitReminder", "   Różnica dni (raw): $daysUntil")

                if (daysUntil < 0) {
                    daysUntil += 7 // Następny tydzień
                    android.util.Log.d("HabitReminder", "   Korekta (minęło): $daysUntil")
                } else if (daysUntil == 0) {
                    // Dzisiaj - sprawdź czy czas już minął
                    val now = LocalTime.now()
                    android.util.Log.d("HabitReminder", "   DZISIAJ! Teraz: $now, Cel: $time")

                    if (time.isBefore(now) || time == now) {
                        daysUntil = 7 // Następny tydzień
                        android.util.Log.d("HabitReminder", "   Czas minął, następny tydzień: $daysUntil")
                    } else {
                        android.util.Log.d("HabitReminder", "   Czas jeszcze nie minął - dzisiaj!")
                    }
                }

                val nextDate = today.plusDays(daysUntil.toLong())
                val nextDateTime = LocalDateTime.of(nextDate, time)
                val triggerTime = nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                android.util.Log.d("HabitReminder", "   Następna data: $nextDate")
                android.util.Log.d("HabitReminder", "   Następny DateTime: $nextDateTime")
                android.util.Log.d("HabitReminder", "   TriggerTime (ms): $triggerTime")
                android.util.Log.d("HabitReminder", "   Obecny czas (ms): ${System.currentTimeMillis()}")
                android.util.Log.d("HabitReminder", "   Różnica (s): ${(triggerTime - System.currentTimeMillis()) / 1000}")

                val intent = createIntent(context, habit).apply {
                    putExtra("target_day", dayName)
                }

                // ✅ Unikalny ID dla każdego dnia tygodnia
                val uniqueId = "${habit.id}_$dayName".hashCode()
                android.util.Log.d("HabitReminder", "   Unikalny ID: $uniqueId")

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    uniqueId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // ✅ ZMIANA: Użyj setExactAndAllowWhileIdle zamiast setRepeating
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }

                android.util.Log.d("HabitReminder", "   ✅ ZAPLANOWANO (exact): $dayName za $daysUntil dni")
            } catch (e: Exception) {
                android.util.Log.e("HabitReminder", "   ❌ BŁĄD dla $dayName: ${e.message}", e)
            }
        }

        android.util.Log.d("HabitReminder", "   ───────────────────")
        android.util.Log.d("HabitReminder", "   PODSUMOWANIE: ${habit.weeklyDays.size} dni zaplanowano")
    }

    /**
     * Miesięczny nawyk - osobne alarmy na każdy wybrany dzień miesiąca
     */
    private fun scheduleMonthlyReminders(
        context: Context,
        alarmManager: AlarmManager,
        habit: Habit,
        time: LocalTime
    ) {
        val today = LocalDate.now()

        habit.monthlyDates.forEach { dateString ->
            try {
                val dayOfMonth = dateString.toIntOrNull() ?: return@forEach
                if (dayOfMonth !in 1..31) return@forEach

                // Znajdź następne wystąpienie tego dnia
                var nextDate = today.withDayOfMonth(
                    dayOfMonth.coerceAtMost(today.lengthOfMonth())
                )

                // Jeśli data już minęła w tym miesiącu, idź do następnego
                if (nextDate.isBefore(today) ||
                    (nextDate == today && LocalTime.now().isAfter(time))) {
                    nextDate = nextDate.plusMonths(1).withDayOfMonth(
                        dayOfMonth.coerceAtMost(nextDate.plusMonths(1).lengthOfMonth())
                    )
                }

                val nextDateTime = LocalDateTime.of(nextDate, time)
                val triggerTime = nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val intent = createIntent(context, habit).apply {
                    putExtra("target_day_of_month", dayOfMonth)
                }

                // ✅ Unikalny ID dla każdego dnia miesiąca
                val uniqueId = "${habit.id}_day$dayOfMonth".hashCode()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    uniqueId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // ✅ Pojedynczy alarm (reschedule w Receiver)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )

                android.util.Log.d(
                    "HabitReminder",
                    "   Zaplanowano: dzień $dayOfMonth (${nextDate}, ID: $uniqueId)"
                )
            } catch (e: Exception) {
                android.util.Log.e("HabitReminder", "❌ Błąd dla dnia $dateString: ${e.message}")
            }
        }

        android.util.Log.d("HabitReminder", "   Typ: Miesięczny (${habit.monthlyDates.size} dni)")
    }

    /**
     * Anuluj WSZYSTKIE przypomnienia dla nawyku
     */
    fun cancel(context: Context, habit: Habit) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Anuluj główny alarm (dla codziennych)
            val mainIntent = createIntent(context, habit)
            val mainPendingIntent = createPendingIntent(context, habit, mainIntent)
            alarmManager.cancel(mainPendingIntent)
            mainPendingIntent.cancel()

            // Anuluj alarmy tygodniowe
            habit.weeklyDays.forEach { dayName ->
                val uniqueId = "${habit.id}_$dayName".hashCode()
                val intent = createIntent(context, habit)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    uniqueId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }

            // Anuluj alarmy miesięczne
            habit.monthlyDates.forEach { dateString ->
                val dayOfMonth = dateString.toIntOrNull() ?: return@forEach
                val uniqueId = "${habit.id}_day$dayOfMonth".hashCode()
                val intent = createIntent(context, habit)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    uniqueId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }

            android.util.Log.d("HabitReminder", "🗑️ Anulowano wszystkie alarmy: ${habit.name}")
        } catch (e: Exception) {
            android.util.Log.e("HabitReminder", "❌ Błąd anulowania: ${e.message}", e)
        }
    }

    fun reschedule(context: Context, habit: Habit) {
        cancel(context, habit)
        schedule(context, habit)
    }

    // Helper functions
    private fun createIntent(context: Context, habit: Habit): Intent {
        return Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("habit_id", habit.id)
            putExtra("habit_name", habit.name)
            putExtra("reminder_time", habit.reminderTime) // ✅ DODANE
            // Dodaj informacje o typie nawyku
            putExtra("is_weekly", habit.weeklyDays.isNotEmpty())
            putExtra("is_monthly", habit.monthlyDates.isNotEmpty())
            putExtra("weekly_days", habit.weeklyDays.toTypedArray())
            putExtra("monthly_dates", habit.monthlyDates.toTypedArray())
        }
    }

    private fun createPendingIntent(context: Context, habit: Habit, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}