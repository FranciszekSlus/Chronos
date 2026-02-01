// Plik: workers/HabitResetWorker.kt
package com.tasker.chronos.workers

import android.content.Context
import androidx.work.*
import com.tasker.chronos.data.repository.HabitsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Worker do resetowania statusu nawyków o północy
 * Oblicza streaks i resetuje completion status
 */
class HabitResetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        /**
         * Planuje codzienne resetowanie o 00:00
         */
        fun scheduleMidnightReset(context: Context) {
            val currentTime = LocalTime.now()
            val midnight = LocalTime.MIDNIGHT

            // Oblicz czas do najbliższej północy
            val initialDelay = if (currentTime.isBefore(midnight)) {
                Duration.between(currentTime, midnight).toMinutes()
            } else {
                Duration.between(currentTime, midnight.plusHours(24)).toMinutes()
            }

            val workRequest = PeriodicWorkRequestBuilder<HabitResetWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .addTag("midnight_reset")
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false) // Wykonaj nawet przy niskiej baterii
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "midnight_habit_reset",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )

            android.util.Log.d(
                "HabitResetWorker",
                "✅ Zaplanowano reset o północy (za ${initialDelay}min)"
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d("HabitResetWorker", "🌙 Rozpoczynam reset o północy...")

            val repository = HabitsRepository(applicationContext)
            val habits = repository.getAllHabitsAsList()

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            habits.forEach { habit ->
                // Sprawdź czy nawyk był aktywny wczoraj
                val wasActiveYesterday = when {
                    habit.weeklyDays.isNotEmpty() -> habit.weeklyDays.contains(yesterday.dayOfWeek.name)
                    habit.monthlyDates.isNotEmpty() -> habit.monthlyDates.contains(yesterday.dayOfMonth.toString())
                    else -> true // Codzienny
                }

                if (wasActiveYesterday) {
                    val wasCompletedYesterday = habit.completionDates.contains(yesterday.toString())

                    // ✅ NOWA LOGIKA: NIE zmieniaj streak - tylko resetuj jeśli nie zaznaczono
                    val newStreak = if (wasCompletedYesterday) {
                        habit.streak  // ✅ Zachowaj obecny streak (był zaznaczony)
                    } else {
                        0  // ❌ Reset - nie zaznaczono wczoraj
                    }

                    // ✅ Zaktualizuj TYLKO jeśli streak się zmienił
                    if (newStreak != habit.streak) {
                        val updatedHabit = habit.copy(
                            streak = newStreak,
                            lastCompletionDate = if (wasCompletedYesterday) yesterday.toString() else habit.lastCompletionDate
                        )

                        repository.updateHabit(updatedHabit)

                        android.util.Log.d(
                            "HabitResetWorker",
                            "🔄 ${habit.name}: streak ${habit.streak} → $newStreak (wczoraj ${if (wasCompletedYesterday) "✅" else "❌"})"
                        )
                    } else {
                        android.util.Log.d(
                            "HabitResetWorker",
                            "✅ ${habit.name}: streak = ${habit.streak} (bez zmian)"
                        )
                    }
                } else {
                    android.util.Log.d(
                        "HabitResetWorker",
                        "⏭️ ${habit.name}: wczoraj nieaktywny, pomijam"
                    )
                }
            }

            android.util.Log.d("HabitResetWorker", "✅ Reset zakończony pomyślnie")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("HabitResetWorker", "❌ Błąd resetu: ${e.message}", e)
            Result.retry()
        }
    }
}