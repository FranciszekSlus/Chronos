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
import kotlinx.coroutines.flow.first
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



    // ✅ NOWY KOD:
    // ✅ DODAJ FLAGĘ na górze klasy (po companion object):
    companion object {
        const val POINTS_PER_DAY = 10
    }

    private var hasCheckedStreaksOnStartup = false  // ✅ DODAJ TO

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllHabits().collect { habitsFromDb ->
                _habits.value = habitsFromDb
                // ✅ Sprawdź i zresetuj streaki tylko raz, przy pierwszym załadowaniu
                if (!hasCheckedStreaksOnStartup && habitsFromDb.isNotEmpty()) {
                    checkAndResetStreaksOnStartup()
                    hasCheckedStreaksOnStartup = true
                }
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
            val yesterday = today.minusDays(1)
            val yesterdayString = yesterday.toString()

            if (!isHabitActiveOn(habitToUpdate, today)) {
                android.util.Log.d("HabitsViewModel", "⚠️ Nawyk nieaktywny dzisiaj")
                return@launch
            }

            val currentCompletionDates = habitToUpdate.completionDates
            val isCompletedToday = currentCompletionDates.contains(todayString)

            val updatedHabit = if (isCompletedToday) {
                // ✅ ODZNACZANIE - COFNIJ STREAK O 1
                val updatedDates = currentCompletionDates.minus(todayString)
                userProfileRepository.addPoints(-POINTS_PER_DAY)

                // ✅ OBLICZ POPRZEDNI STREAK
                val wasCompletedYesterday = currentCompletionDates.contains(yesterdayString)
                val wasYesterdayActive = isHabitActiveOn(habitToUpdate, yesterday)

                // Jeśli wczoraj było zaznaczone i aktywne → cofnij o 1
                // W przeciwnym razie (dzisiejszy był pierwszy w serii) → 0
                val previousStreak = if (!wasYesterdayActive || wasCompletedYesterday) {
                    (habitToUpdate.streak - 1).coerceAtLeast(0)
                } else {
                    0  // Dzisiejszy był pierwszy po przerwie
                }

                habitToUpdate.copy(
                    completionDates = updatedDates,
                    points = (habitToUpdate.points - POINTS_PER_DAY).coerceAtLeast(0),
                    streak = previousStreak,  // ✅ COFNIJ O 1
                    lastCompletionDate = updatedDates.maxOrNull()
                )
            } else {
                // ✅ ZAZNACZANIE
                val updatedDates = currentCompletionDates.plus(todayString)
                userProfileRepository.addPoints(POINTS_PER_DAY)

                withContext(Dispatchers.Main) {
                    _showConfettiForHabit.value = habitId
                }

                // ✅ PROSTA LOGIKA STREAKA
                val wasCompletedYesterday = currentCompletionDates.contains(yesterdayString)
                val wasYesterdayActive = isHabitActiveOn(habitToUpdate, yesterday)

                val newStreak = when {
                    // Wczoraj nieaktywny (weekend/wolne) → kontynuuj
                    !wasYesterdayActive -> habitToUpdate.streak + 1

                    // Wczoraj aktywny I zaznaczony → +1
                    wasYesterdayActive && wasCompletedYesterday -> habitToUpdate.streak + 1

                    // Wczoraj aktywny ALE NIE zaznaczony → reset do 1
                    else -> 1
                }

                habitToUpdate.copy(
                    completionDates = updatedDates,
                    points = habitToUpdate.points + POINTS_PER_DAY,
                    streak = newStreak,
                    lastCompletionDate = todayString
                )
            }

            repository.updateHabit(updatedHabit)
            _habits.value = currentHabits.map { if (it.id == habitId) updatedHabit else it }

            android.util.Log.d(
                "HabitsViewModel",
                if (isCompletedToday) "↩️ Odznaczono: ${habitToUpdate.name}, streak: ${updatedHabit.streak}"
                else "✅ Zaznaczono: ${habitToUpdate.name}, streak: ${updatedHabit.streak} 🔥"
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
    // ✅ DODAJ PRZED init {} (około linii 30):
    // ✅ ZAMIEŃ NA TO (przed init {}):
    private fun checkAndResetStreaksOnStartup() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val habitsFromDb = _habits.value

            val habitsToUpdate = habitsFromDb.mapNotNull { habit ->
                if (habit.streak == 0) return@mapNotNull null

                // ✅ Sprawdź TYLKO czy wczoraj było zaznaczone (jeśli wczoraj był aktywny dzień)
                val wasYesterdayActive = isHabitActiveOn(habit, yesterday)

                if (!wasYesterdayActive) {
                    // Wczoraj nieaktywny (weekend/wolne) → streak OK, nie resetuj
                    android.util.Log.d("HabitsViewModel",
                        "✅ '${habit.name}' - wczoraj nieaktywny, streak zachowany: ${habit.streak}")
                    return@mapNotNull null
                }

                // Wczoraj był aktywny - sprawdź czy zaznaczony
                val wasCompletedYesterday = habit.completionDates.contains(yesterday.toString())

                if (!wasCompletedYesterday) {
                    // ❌ Wczoraj aktywny ale NIE zaznaczony → RESET
                    android.util.Log.d("HabitsViewModel",
                        "🔄 Reset streak dla '${habit.name}' (wczoraj nie zaznaczono)")
                    habit.copy(streak = 0)
                } else {
                    // ✅ Wczoraj zaznaczony → streak OK
                    android.util.Log.d("HabitsViewModel",
                        "✅ '${habit.name}' - wczoraj zaznaczony, streak: ${habit.streak}")
                    null
                }
            }

            if (habitsToUpdate.isNotEmpty()) {
                repository.updateHabits(habitsToUpdate)
                val updatedHabitsMap = habitsToUpdate.associateBy { it.id }
                _habits.value = _habits.value.map { updatedHabitsMap[it.id] ?: it }
                android.util.Log.d("HabitsViewModel", "✅ Zresetowano ${habitsToUpdate.size} streaki")
            } else {
                android.util.Log.d("HabitsViewModel", "✅ Wszystkie streaki OK, brak resetów")
            }
        }
    }



}
