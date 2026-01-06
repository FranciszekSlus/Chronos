// Plik: data/repository/EventsRepository.kt
package com.tasker.chronos.data.repository

import android.content.Context
import com.tasker.chronos.data.models.CalendarEvent
import com.tasker.chronos.data.models.EventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * Repository do zarządzania wydarzeniami w kalendarzu
 * Agreguje dane z HabitsRepository, TasksRepository i GoalsRepository
 */
class EventsRepository(private val context: Context) {

    private val habitsRepository = HabitsRepository(context)
    private val tasksRepository = TasksRepository(context)
    private val goalsRepository = GoalsRepository(context)

    /**
     * Pobiera wszystkie wydarzenia z nawyków, zadań i celów
     */
    fun getAllCalendarEvents(): Flow<List<CalendarEvent>> {
        return combine(
            habitsRepository.getAllHabits(),
            tasksRepository.getAllTasks(),
            goalsRepository.getAllGoals()
        ) { habits, tasks, goals ->

            val events = mutableListOf<CalendarEvent>()
            val today = LocalDate.now()
            val startDate = today.minusMonths(1)
            val endDate = today.plusMonths(2)

            // Wydarzenia z nawyków
            habits.forEach { habit ->
                var currentDate = startDate

                while (!currentDate.isAfter(endDate)) {
                    val isActiveDay = when {
                        habit.weeklyDays.isNotEmpty() -> {
                            habit.weeklyDays.contains(currentDate.dayOfWeek.name)
                        }
                        habit.monthlyDates.isNotEmpty() -> {
                            habit.monthlyDates.contains(currentDate.dayOfMonth.toString())
                        }
                        else -> true // Codzienny
                    }

                    if (isActiveDay) {
                        events.add(
                            CalendarEvent(
                                id = "${habit.id}_${currentDate}",
                                title = habit.name,
                                date = currentDate.toString(),
                                startTime = habit.reminderTime,
                                type = EventType.HABIT,
                                sourceId = habit.id,
                                isCompleted = habit.completionDates.contains(currentDate.toString()),
                                hasReminder = habit.hasReminder,
                                reminderTime = habit.reminderTime
                            )
                        )
                    }

                    currentDate = currentDate.plusDays(1)
                }
            }

            // Wydarzenia z zadań
            tasks.forEach { task ->
                if (task.date != null) {
                    events.add(
                        CalendarEvent(
                            id = task.id,
                            title = task.title,
                            date = task.date,
                            startTime = task.time,
                            type = EventType.TASK,
                            sourceId = task.id,
                            isCompleted = task.isCompleted,
                            hasReminder = task.hasReminder,
                            reminderTime = task.time
                        )
                    )
                }
            }

            // Wydarzenia z celów (mini-cele z datami + deadline)
            goals.forEach { goal ->
                // Mini-cele
                goal.miniGoals.forEach { miniGoal ->
                    if (miniGoal.date != null) {
                        events.add(
                            CalendarEvent(
                                id = "${goal.id}_${miniGoal.id}",
                                title = "${goal.title}: ${miniGoal.title}",
                                date = miniGoal.date,
                                type = EventType.GOAL,
                                sourceId = goal.id,
                                isCompleted = miniGoal.isCompleted
                            )
                        )
                    }
                }

                // Deadline celu
                if (goal.endDate != null) {
                    events.add(
                        CalendarEvent(
                            id = "${goal.id}_deadline",
                            title = "📌 ${goal.title} (termin)",
                            date = goal.endDate,
                            type = EventType.GOAL,
                            sourceId = goal.id,
                            isCompleted = goal.isCompleted()
                        )
                    )
                }
            }

            events
        }
    }
}