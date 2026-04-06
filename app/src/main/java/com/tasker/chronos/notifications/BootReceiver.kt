// Plik: notifications/BootReceiver.kt
package com.tasker.chronos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tasker.chronos.data.repository.HabitsRepository
import com.tasker.chronos.data.repository.GoalsRepository
import com.tasker.chronos.data.repository.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        android.util.Log.d("BootReceiver", "📱 Boot detected - przywracam powiadomienia")

        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                // ✅ Przywróć powiadomienia nawyków
                val habitsRepo = HabitsRepository(context)
                val habits = habitsRepo.getAllHabits().first()
                habits.forEach { habit ->
                    if (habit.hasReminder && !habit.reminderTime.isNullOrBlank()) {
                        HabitReminderScheduler.schedule(context, habit)
                        android.util.Log.d("BootReceiver", "✅ Przywrócono nawyk: ${habit.name}")
                    }
                }

                // ✅ Przywróć powiadomienia zadań
                val tasksRepo = TasksRepository(context)
                val tasks = tasksRepo.getAllTasks().first()
                tasks.forEach { task ->
                    if (task.hasReminder && task.date != null && !task.isCompleted) {
                        TaskReminderScheduler.schedule(context, task)
                        android.util.Log.d("BootReceiver", "✅ Przywrócono zadanie: ${task.title}")
                    }
                }

                // ✅ Przywróć powiadomienia celów
                val goalsRepo = GoalsRepository(context)
                val goals = goalsRepo.getAllGoals().first()
                goals.forEach { goal ->
                    if (!goal.isCompleted()) {
                        GoalReminderScheduler.schedule(context, goal)
                        if (goal.hasPeriodicReminder) {
                            GoalReminderScheduler.schedulePeriodicReminder(context, goal)
                        }
                        android.util.Log.d("BootReceiver", "✅ Przywrócono cel: ${goal.title}")
                    }
                }

                android.util.Log.d("BootReceiver", "✅ Wszystkie powiadomienia przywrócone")
            } catch (e: Exception) {
                android.util.Log.e("BootReceiver", "❌ Błąd przywracania: ${e.message}", e)
            }
        }
    }
}