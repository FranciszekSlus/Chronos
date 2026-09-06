package com.tasker.chronos.notifications

import android.content.Context
import com.tasker.chronos.data.repository.CustomEventsRepository
import com.tasker.chronos.data.repository.GoalsRepository
import com.tasker.chronos.data.repository.HabitsRepository
import com.tasker.chronos.data.repository.TasksRepository
import kotlinx.coroutines.flow.first

/**
 * Restores all reminder alarms after boot, timezone change, or app update.
 */
object ReminderRescheduler {

    suspend fun rescheduleAll(context: Context) {
        android.util.Log.d("ReminderRescheduler", "🔄 Rescheduling all reminders...")

        try {
            val tasks = TasksRepository(context).getAllTasksAsList()
            tasks.filter { it.hasReminder && !it.isCompleted }.forEach { task ->
                TaskReminderScheduler.reschedule(context, task)
            }
            android.util.Log.d("ReminderRescheduler", "   Tasks: ${tasks.count { it.hasReminder }}")
        } catch (e: Exception) {
            android.util.Log.e("ReminderRescheduler", "Tasks failed: ${e.message}", e)
        }

        try {
            val habits = HabitsRepository(context).getAllHabitsAsList()
            habits.filter { it.hasReminder }.forEach { habit ->
                HabitReminderScheduler.reschedule(context, habit)
            }
            android.util.Log.d("ReminderRescheduler", "   Habits: ${habits.count { it.hasReminder }}")
        } catch (e: Exception) {
            android.util.Log.e("ReminderRescheduler", "Habits failed: ${e.message}", e)
        }

        try {
            val goals = GoalsRepository(context).getAllGoalsAsList()
            goals.filter { !it.manuallyCompleted }.forEach { goal ->
                GoalReminderScheduler.reschedule(context, goal)
                if (goal.hasPeriodicReminder) {
                    GoalReminderScheduler.schedulePeriodicReminder(context, goal)
                }
            }
            android.util.Log.d("ReminderRescheduler", "   Goals: ${goals.size}")
        } catch (e: Exception) {
            android.util.Log.e("ReminderRescheduler", "Goals failed: ${e.message}", e)
        }

        try {
            val events = CustomEventsRepository(context).getAllCustomEvents().first()
            events.forEach { event ->
                CustomEventScheduler.reschedule(context, event)
            }
            android.util.Log.d("ReminderRescheduler", "   Events: ${events.size}")
        } catch (e: Exception) {
            android.util.Log.e("ReminderRescheduler", "Events failed: ${e.message}", e)
        }

        android.util.Log.d("ReminderRescheduler", "✅ Reschedule complete")
    }
}
