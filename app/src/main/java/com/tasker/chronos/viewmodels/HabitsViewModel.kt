package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.models.Habit
import com.tasker.chronos.data.repository.HabitsRepository
import com.tasker.chronos.data.repository.UserProfileRepository
import com.tasker.chronos.notifications.HabitReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class HabitsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitsRepository(application)
    private val userProfileRepository = UserProfileRepository(application)

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits = _habits.asStateFlow() // POPRAWKA: Dodaj publiczny dostęp

    private val _showConfettiForHabit = MutableStateFlow<String?>(null)
    val showConfettiForHabit = _showConfettiForHabit.asStateFlow()

    companion object {
        const val POINTS_PER_DAY = 10
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllHabits().collect { habitsFromDb ->
                _habits.value = habitsFromDb
            }
        }
    }

    // Helper: Czy nawyk jest aktywny danego dnia
    private fun isHabitActiveOn(habit: Habit, date: LocalDate): Boolean {
        return when {
            habit.weeklyDays.isNotEmpty() -> habit.weeklyDays.contains(date.dayOfWeek.name)
            habit.monthlyDates.isNotEmpty() -> habit.monthlyDates.any { it.toIntOrNull() == date.dayOfMonth }
            else -> true
        }
    }

    // Helper: Czy nawyk jest ukończony danego dnia
    private fun isCompletedOn(habit: Habit, date: LocalDate): Boolean {
        return habit.completionDates.contains(date.toString())
    }

    // Helper: Czy to nawyk codzienny
    private fun isHabitDaily(habit: Habit) = habit.weeklyDays.isEmpty() && habit.monthlyDates.isEmpty()

    fun addHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addHabit(habit)
            if (habit.hasReminder && !habit.reminderTime.isNullOrBlank()) {
                HabitReminderScheduler.schedule(getApplication(), habit)
            }
        }
    }

    fun toggleHabitCompleted(habitId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentHabits = _habits.value
            val habitToUpdate = currentHabits.find { it.id == habitId } ?: return@launch

            val today = LocalDate.now()
            val todayString = today.toString()

            if (!isHabitActiveOn(habitToUpdate, today)) {
                android.util.Log.d("HabitsViewModel", "⚠️ Nawyk nieaktywny dzisiaj")
                return@launch
            }

            val currentCompletionDates = habitToUpdate.completionDates
            val isCompletedToday = currentCompletionDates.contains(todayString)

            val updatedHabit = if (isCompletedToday) {
                // ODZNACZANIE
                val updatedDates = currentCompletionDates.minus(todayString)
                userProfileRepository.addPoints(-POINTS_PER_DAY)

                habitToUpdate.copy(
                    completionDates = updatedDates,
                    points = (habitToUpdate.points - POINTS_PER_DAY).coerceAtLeast(0)
                )
            } else {
                // ZAZNACZANIE
                val updatedDates = currentCompletionDates.plus(todayString)
                userProfileRepository.addPoints(POINTS_PER_DAY)

                // Confetti na głównym wątku
                withContext(Dispatchers.Main) {
                    _showConfettiForHabit.value = habitId
                }

                habitToUpdate.copy(
                    completionDates = updatedDates,
                    points = habitToUpdate.points + POINTS_PER_DAY
                )
            }

            repository.updateHabit(updatedHabit)
            _habits.value = currentHabits.map { if (it.id == habitId) updatedHabit else it }

            android.util.Log.d(
                "HabitsViewModel",
                if (isCompletedToday) "↩️ Odznaczono: ${habitToUpdate.name}"
                else "✅ Zaznaczono: ${habitToUpdate.name}"
            )
        }
    }

    fun resetConfetti() {
        _showConfettiForHabit.value = null
    }

    fun updateHabit(updatedHabit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateHabit(updatedHabit)
            _habits.value = _habits.value.map {
                if (it.id == updatedHabit.id) updatedHabit else it
            }

            if (updatedHabit.hasReminder && !updatedHabit.reminderTime.isNullOrBlank()) {
                HabitReminderScheduler.reschedule(getApplication(), updatedHabit)
            } else {
                HabitReminderScheduler.cancel(getApplication(), updatedHabit)
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHabit(habit.id)
            HabitReminderScheduler.cancel(getApplication(), habit)
        }
    }
}