// Plik: viewmodels/CalendarViewModel.kt
package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.models.CalendarEvent
import com.tasker.chronos.data.models.EventType
import com.tasker.chronos.data.models.Habit
import com.tasker.chronos.data.repository.HabitsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val habitsRepository = HabitsRepository(application)
    private val tasksRepository = com.tasker.chronos.data.repository.TasksRepository(application)
    private val goalsRepository = com.tasker.chronos.data.repository.GoalsRepository(application)

    // Aktualnie wybrany dzień
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    // Aktualnie wyświetlany miesiąc
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    // Wszystkie wydarzenia w kalendarzu
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events = _events.asStateFlow()

    // Wydarzenia dla wybranego dnia
    val eventsForSelectedDay: StateFlow<List<CalendarEvent>> = combine(
        _selectedDate,
        _events
    ) { date, events ->
        events.filter { it.date == date.toString() }
            .sortedBy { it.startTime ?: "00:00" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Mapa: dzień -> lista typów wydarzeń (dla kolorowych kropek)
    val eventTypesPerDay: StateFlow<Map<String, List<EventType>>> = _events
        .map { events ->
            events.groupBy { it.date }
                .mapValues { (_, dayEvents) ->
                    dayEvents.map { it.type }.distinct()
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    init {
        // Ładuj nawyki i zadania, generuj wydarzenia
        viewModelScope.launch(Dispatchers.IO) {
            habitsRepository.getAllHabits().collect { habits ->
                val tasks = tasksRepository.getAllTasks().first()
                generateEvents(habits, tasks)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            tasksRepository.getAllTasks().collect { tasks ->
                val habits = habitsRepository.getAllHabits().first()
                generateEvents(habits, tasks)
            }
        }
    }

    /**
     * Generuje wydarzenia w kalendarzu na podstawie nawyków i zadań
     */
    private fun generateEvents(habits: List<Habit>, tasks: List<com.tasker.chronos.data.models.Task>) {
        val today = LocalDate.now()
        val startDate = today.minusMonths(1)
        val endDate = today.plusMonths(2)

        val calendarEvents = mutableListOf<CalendarEvent>()

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
                    else -> true
                }

                if (isActiveDay) {
                    calendarEvents.add(
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
                calendarEvents.add(
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

        // TODO: Dodaj cele gdy będą gotowe

        _events.value = calendarEvents
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _selectedMonth.value = YearMonth.from(today)
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun previousWeek() {
        _selectedDate.value = _selectedDate.value.minusWeeks(1)
    }

    fun nextWeek() {
        _selectedDate.value = _selectedDate.value.plusWeeks(1)
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }
}