// Plik: viewmodels/GoalsViewModel.kt
package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.models.Goal
import com.tasker.chronos.data.models.MiniGoal
import com.tasker.chronos.data.repository.GoalsRepository
import com.tasker.chronos.notifications.GoalReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GoalsRepository(application)

    // Wszystkie cele
    val allGoals: StateFlow<List<Goal>> = repository.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Aktywne cele (nie ukończone)
    val activeGoals: StateFlow<List<Goal>> = repository.getActiveGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )



    fun addGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGoal(goal)
            GoalReminderScheduler.schedule(getApplication(), goal)
            android.util.Log.d("GoalsViewModel", "✅ Dodano cel: ${goal.title}")
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGoal(goal)
            GoalReminderScheduler.reschedule(getApplication(), goal)
            android.util.Log.d("GoalsViewModel", "✅ Zaktualizowano cel: ${goal.title}")
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(goal.id)
            GoalReminderScheduler.cancel(getApplication(), goal)
            android.util.Log.d("GoalsViewModel", "🗑️ Usunięto cel: ${goal.title}")
        }
    }

    /**
     * Przełącz status celu (dla celów BEZ mini-celów)
     */
    fun toggleGoalCompleted(goalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // ✅ POPRAWKA: Pobierz aktualny stan z Repository
            val allGoalsList = repository.getAllGoals().first()
            val goal = allGoalsList.find { it.id == goalId }

            if (goal == null) {
                android.util.Log.e("GoalsViewModel", "❌ Nie znaleziono celu: $goalId")
                return@launch
            }

            // Tylko dla celów bez mini-celów
            if (goal.miniGoals.isEmpty()) {
                val isNowCompleted = !goal.isCompleted()
                val updatedGoal = goal.copy(
                    miniGoals = listOf(
                        MiniGoal(
                            title = "Ukończ cel",
                            isCompleted = isNowCompleted,
                            completedAt = if (isNowCompleted)
                                LocalDateTime.now().toString()
                            else null
                        )
                    )
                )

                repository.updateGoal(updatedGoal)
                android.util.Log.d(
                    "GoalsViewModel",
                    if (isNowCompleted) "✅ Ukończono cel: ${goal.title}"
                    else "↩️ Przywrócono cel: ${goal.title}"
                )
            }
        }
    }

    /**
     * Przełącz status mini-celu
     */
    fun toggleMiniGoalCompleted(goalId: String, miniGoalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("GoalsViewModel", "🔍 Szukam celu: $goalId")
            android.util.Log.d("GoalsViewModel", "🔍 Mini-cel ID: $miniGoalId")

            val allGoalsList = repository.getAllGoals().first()
            android.util.Log.d("GoalsViewModel", "🔍 Liczba celów w repo: ${allGoalsList.size}")

            val goal = allGoalsList.find { it.id == goalId }

            if (goal == null) {
                android.util.Log.e("GoalsViewModel", "❌ Nie znaleziono celu: $goalId")
                return@launch
            }

            android.util.Log.d("GoalsViewModel", "✅ Znaleziono cel: ${goal.title}")
            android.util.Log.d("GoalsViewModel", "✅ Liczba mini-celów: ${goal.miniGoals.size}")

            // ❌ USUŃ stary kod sprawdzający `miniGoal.date == null`
            // ✅ DODAJ nowy kod:

            val updatedMiniGoals = goal.miniGoals.map { miniGoal ->
                if (miniGoal.id == miniGoalId) {
                    if (miniGoal.date != null) {
                        // Mini-cel Z DATĄ - przełącz isCompleted
                        val newCompletedState = !miniGoal.isCompleted
                        miniGoal.copy(
                            isCompleted = newCompletedState,
                            completedAt = if (newCompletedState)
                                java.time.LocalDateTime.now().toString()
                            else
                                null
                        )
                    } else {
                        // Mini-cel BEZ DATY - przełącz dailyCompletions (dzisiejsza data)
                        val currentCompletions = miniGoal.dailyCompletions ?: emptyList()
                        val today = java.time.LocalDate.now().toString()

                        val newCompletions = if (currentCompletions.contains(today)) {
                            currentCompletions - today  // Usuń
                        } else {
                            currentCompletions + today  // Dodaj
                        }

                        miniGoal.copy(dailyCompletions = newCompletions)
                    }
                } else {
                    miniGoal
                }
            }

            val updatedGoal = goal.copy(miniGoals = updatedMiniGoals)
            repository.updateGoal(updatedGoal)

            // Log
            val miniGoal = updatedMiniGoals.find { it.id == miniGoalId }
            val isCompleted = if (miniGoal?.date != null)
                miniGoal.isCompleted
            else
                miniGoal?.isCompletedToday() ?: false

            android.util.Log.d(
                "GoalsViewModel",
                if (isCompleted)
                    "✅ Ukończono mini-cel: ${miniGoal?.title} w: ${goal.title}"
                else
                    "↩️ Przywrócono mini-cel: ${miniGoal?.title} w: ${goal.title}"
            )
        }
    }

    /**
     * Dodaj mini-cel do celu
     */
    fun addMiniGoal(goalId: String, miniGoal: MiniGoal) {
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("GoalsViewModel", "➕ Dodawanie mini-celu: ${miniGoal.title} do celu: $goalId")

            // ✅ POPRAWKA: Pobierz aktualny stan
            val allGoalsList = repository.getAllGoals().first()
            val goal = allGoalsList.find { it.id == goalId }

            if (goal == null) {
                android.util.Log.e("GoalsViewModel", "❌ Nie znaleziono celu: $goalId")
                return@launch
            }

            val updatedMiniGoals = goal.miniGoals + miniGoal
            val updatedGoal = goal.copy(miniGoals = updatedMiniGoals)

            repository.updateGoal(updatedGoal)
            android.util.Log.d("GoalsViewModel", "✅ Dodano mini-cel: ${miniGoal.title} do: ${goal.title}")
        }
    }

    /**
     * Usuń mini-cel
     */
    fun deleteMiniGoal(goalId: String, miniGoalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("GoalsViewModel", "🗑️ Usuwanie mini-celu: $miniGoalId z celu: $goalId")

            // ✅ POPRAWKA: Pobierz aktualny stan
            val allGoalsList = repository.getAllGoals().first()
            val goal = allGoalsList.find { it.id == goalId }

            if (goal == null) {
                android.util.Log.e("GoalsViewModel", "❌ Nie znaleziono celu: $goalId")
                return@launch
            }

            val updatedMiniGoals = goal.miniGoals.filter { it.id != miniGoalId }
            val updatedGoal = goal.copy(miniGoals = updatedMiniGoals)

            repository.updateGoal(updatedGoal)
            android.util.Log.d("GoalsViewModel", "✅ Usunięto mini-cel z: ${goal.title}")
        }
    }

    /**
     * Zaktualizuj notatki celu
     */
    fun updateNotes(goalId: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // ✅ POPRAWKA: Pobierz aktualny stan
            val allGoalsList = repository.getAllGoals().first()
            val goal = allGoalsList.find { it.id == goalId }

            if (goal == null) {
                android.util.Log.e("GoalsViewModel", "❌ Nie znaleziono celu: $goalId")
                return@launch
            }

            val updatedGoal = goal.copy(notes = notes)
            repository.updateGoal(updatedGoal)

            android.util.Log.d("GoalsViewModel", "📝 Zaktualizowano notatki: ${goal.title}")
        }
    }
    /**
     * ✅ NOWE: Mini-cele z datami (dla inbox w kalendarzu)
     */
    val miniGoalsWithDates: StateFlow<List<Pair<Goal, MiniGoal>>> = allGoals
        .map { goals ->
            android.util.Log.d("GoalsViewModel", "🔍 Wszystkie cele: ${goals.size}")
            goals.forEach { goal ->
                android.util.Log.d("GoalsViewModel", "  Cel: ${goal.title}, mini-cele: ${goal.miniGoals.size}")
                goal.miniGoals.forEach { mini ->
                    android.util.Log.d("GoalsViewModel", "    Mini: ${mini.title}, data: ${mini.date}, completed: ${mini.isCompleted}")
                }
            }

            goals.flatMap { goal ->
                goal.miniGoals
                    .filter { miniGoal ->
                        val hasDate = miniGoal.date != null
                        val notCompleted = !miniGoal.isCompleted
                        android.util.Log.d("GoalsViewModel", "    Filtr: ${miniGoal.title} - hasDate: $hasDate, notCompleted: $notCompleted")
                        hasDate && notCompleted
                    }
                    .map { miniGoal -> goal to miniGoal }
            }
                .sortedBy { (_, miniGoal) ->
                    try {
                        LocalDate.parse(miniGoal.date)
                    } catch (e: Exception) {
                        LocalDate.MAX
                    }
                }
                .also {
                    android.util.Log.d("GoalsViewModel", "✅ Mini-cele z datami: ${it.size}")
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    // ✅ DODAJ NA KOŃCU GoalsViewModel.kt

    // Archiwalne cele (ukończone w 100%)
    val archivedGoals: StateFlow<List<Goal>> = repository.getArchivedGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * ✅ NOWE: Wyczyść archiwum celów
     */
    fun clearArchive() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearArchive()
            android.util.Log.d("GoalsViewModel", "🗑️ Wyczyszczono archiwum celów")
        }
    }
    /**
     * ✅ POPRAWKA: Mini-cele z datami - pokazuj TYLKO w ich dzień
     */
    /**
     * ✅ Mini-cele dla konkretnej daty
     */
    fun getMiniGoalsForDate(date: String): StateFlow<List<Pair<Goal, MiniGoal>>> = allGoals
        .map { goals ->
            android.util.Log.d("GoalsViewModel", "========================================")
            android.util.Log.d("GoalsViewModel", "🔍 getMiniGoalsForDate($date)")
            android.util.Log.d("GoalsViewModel", "📊 Wszystkie cele: ${goals.size}")

            goals.forEach { goal ->
                android.util.Log.d("GoalsViewModel", "  Cel: '${goal.title}'")
                android.util.Log.d("GoalsViewModel", "    Mini-cele: ${goal.miniGoals.size}")
                goal.miniGoals.forEach { mini ->
                    android.util.Log.d("GoalsViewModel",
                        "      - '${mini.title}' | data: ${mini.date} | isCompleted: ${mini.isCompleted}")
                }
            }

            val filtered = goals.flatMap { goal ->
                goal.miniGoals
                    .filter { miniGoal ->
                        // ✅ NOWA LOGIKA:
                        // 1. Mini-cele Z DATĄ: pokazuj TYLKO jeśli data == dzisiaj I nie ukończone
                        // 2. Mini-cele BEZ DATY: pokazuj ZAWSZE (codzienne) I nie ukończone dzisiaj

                        if (miniGoal.date != null) {
                            // Mini-cel Z DATĄ - sprawdź czy pasuje data
                            val matchesDate = miniGoal.date == date
                            val notCompleted = !miniGoal.isCompleted

                            android.util.Log.d("GoalsViewModel",
                                "    [Z datą] '${miniGoal.title}' - matchesDate($matchesDate), notCompleted($notCompleted)")

                            matchesDate && notCompleted
                        } else {
                            // ✅ Mini-cel BEZ DATY - pokazuj ZAWSZE (jeśli nie ukończony dzisiaj)
                            val notCompletedToday = !miniGoal.isCompletedToday()

                            android.util.Log.d("GoalsViewModel",
                                "    [Bez daty] '${miniGoal.title}' - notCompletedToday($notCompletedToday)")

                            notCompletedToday
                        }
                    }
                    .map { miniGoal -> goal to miniGoal }
            }

            android.util.Log.d("GoalsViewModel", "✅ Wynik: ${filtered.size} mini-celów dla $date")
            filtered.forEach { (goal, mini) ->
                val type = if (mini.date != null) "[Z datą]" else "[Codzienny]"
                android.util.Log.d("GoalsViewModel", "  ✓ $type ${mini.title} (z: ${goal.title})")
            }
            android.util.Log.d("GoalsViewModel", "========================================")

            filtered
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    /**
     * ✅ NOWE: Aktualizuj mini-cel (np. zmień datę)
     */
    fun updateMiniGoal(goalId: String, updatedMiniGoal: MiniGoal) {
        viewModelScope.launch(Dispatchers.IO) {
            val allGoalsList = repository.getAllGoals().first()
            val goal = allGoalsList.find { it.id == goalId }

            if (goal == null) {
                android.util.Log.e("GoalsViewModel", "❌ Nie znaleziono celu: $goalId")
                return@launch
            }

            val updatedMiniGoals = goal.miniGoals.map { miniGoal ->
                if (miniGoal.id == updatedMiniGoal.id) updatedMiniGoal else miniGoal
            }

            val updatedGoal = goal.copy(miniGoals = updatedMiniGoals)
            repository.updateGoal(updatedGoal)

            android.util.Log.d("GoalsViewModel", "✅ Zaktualizowano mini-cel: ${updatedMiniGoal.title}")
        }
    }
    /**
     * ✅ NOWE: Przełącz status mini-celu CODZIENNEGO (bez daty)
     */
    fun toggleDailyMiniGoal(goalId: String, miniGoalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val allGoalsList = repository.getAllGoals().first()
            val goal = allGoalsList.find { it.id == goalId }

            if (goal == null) {
                android.util.Log.e("GoalsViewModel", "❌ Nie znaleziono celu: $goalId")
                return@launch
            }

            val today = LocalDate.now().toString()

            val updatedMiniGoals = goal.miniGoals.map { miniGoal ->
                if (miniGoal.id == miniGoalId && miniGoal.date == null) {
                    // Mini-cel BEZ daty - dodaj/usuń dzisiejszą datę z listy
                    val newCompletions = if (miniGoal.dailyCompletions.contains(today)) {
                        miniGoal.dailyCompletions - today  // Usuń
                    } else {
                        miniGoal.dailyCompletions + today  // Dodaj
                    }

                    miniGoal.copy(dailyCompletions = newCompletions)
                } else {
                    miniGoal
                }
            }

            val updatedGoal = goal.copy(miniGoals = updatedMiniGoals)
            repository.updateGoal(updatedGoal)

            android.util.Log.d("GoalsViewModel", "✅ Przełączono codzienny mini-cel")
        }
    }
    init {
        // ✅ Migracja: Napraw stare mini-cele bez dailyCompletions
        viewModelScope.launch(Dispatchers.IO) {
            val goals = repository.getAllGoals().first()
            var needsUpdate = false

            val updatedGoals = goals.map { goal ->
                val updatedMiniGoals = goal.miniGoals.map { miniGoal ->
                    if (miniGoal.dailyCompletions == null) {
                        needsUpdate = true
                        miniGoal.copy(dailyCompletions = emptyList())
                    } else {
                        miniGoal
                    }
                }

                if (updatedMiniGoals != goal.miniGoals) {
                    goal.copy(miniGoals = updatedMiniGoals)
                } else {
                    goal
                }
            }

            if (needsUpdate) {
                updatedGoals.forEach { goal ->
                    repository.updateGoal(goal)
                }
                android.util.Log.d("GoalsViewModel", "✅ Zmigrowano ${updatedGoals.size} celów")
            }
        }
    }
}
