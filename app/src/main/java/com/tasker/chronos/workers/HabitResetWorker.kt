// Plik: workers/HabitResetWorker.kt
package com.tasker.chronos.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tasker.chronos.data.repository.HabitsRepository
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Worker do resetowania statusu nawykow o polnocy.
 */
class HabitResetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        fun scheduleMidnightReset(context: Context) {
            val initialDelay = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                java.time.LocalDate.now().plusDays(1).atStartOfDay()
            ).toMinutes().coerceAtLeast(1)

            val workRequest = PeriodicWorkRequestBuilder<HabitResetWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .addTag("midnight_reset")
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
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
                "Zaplanowano reset o polnocy (za ${initialDelay}min)"
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d("HabitResetWorker", "Rozpoczynam reset o polnocy...")

            val repository = HabitsRepository(applicationContext)
            val habits = repository.getAllHabitsAsList()
            val yesterday = LocalDate.now().minusDays(1)

            habits.forEach { habit ->
                val wasActiveYesterday = when {
                    habit.weeklyDays.isNotEmpty() -> habit.weeklyDays.contains(yesterday.dayOfWeek.name)
                    habit.monthlyDates.isNotEmpty() -> habit.monthlyDates.contains(yesterday.dayOfMonth.toString())
                    else -> true
                }

                if (wasActiveYesterday) {
                    val wasCompletedYesterday = habit.completionDates.contains(yesterday.toString())
                    val newStreak = if (wasCompletedYesterday) habit.streak else 0

                    if (newStreak != habit.streak) {
                        val updatedHabit = habit.copy(
                            streak = newStreak,
                            lastCompletionDate = if (wasCompletedYesterday) {
                                yesterday.toString()
                            } else {
                                habit.lastCompletionDate
                            }
                        )
                        repository.updateHabit(updatedHabit)
                        android.util.Log.d(
                            "HabitResetWorker",
                            "${habit.name}: streak ${habit.streak} -> $newStreak"
                        )
                    }
                }
            }

            android.util.Log.d("HabitResetWorker", "Reset zakonczony pomyslnie")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("HabitResetWorker", "Blad resetu: ${e.message}", e)
            Result.retry()
        }
    }
}
