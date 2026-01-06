// Plik: data/models/HabitFilters.kt
package com.tasker.chronos.data.models

data class HabitFilters(
    val searchQuery: String = "",
    val frequencies: Set<HabitFrequency> = emptySet(), // Codzienny/Tygodniowy/Miesięczny
    val showCompleted: Boolean = true, // Pokaż ukończone dzisiaj
    val minStreak: Int? = null, // Minimum streak
    val sortBy: HabitSortOption = HabitSortOption.NAME_ASC
)


enum class HabitSortOption {
    NAME_ASC,       // A-Z
    NAME_DESC,      // Z-A
    STREAK_HIGH,    // Najdłuższy streak
    STREAK_LOW,     // Najkrótszy streak
    POINTS_HIGH,    // Najwięcej punktów
    POINTS_LOW      // Najmniej punktów
}

fun List<Habit>.applyFilters(filters: HabitFilters): List<Habit> {
    var result = this

    // Wyszukiwanie
    if (filters.searchQuery.isNotBlank()) {
        result = result.filter {
            it.name.contains(filters.searchQuery, ignoreCase = true)
        }
    }

    // Częstotliwość
    if (filters.frequencies.isNotEmpty()) {
        result = result.filter { habit ->
            when {
                HabitFrequency.CODZIENNY in filters.frequencies &&
                        habit.weeklyDays.isEmpty() && habit.monthlyDates.isEmpty() -> true
                    HabitFrequency.TYGODNIOWY in filters.frequencies &&
                        habit.weeklyDays.isNotEmpty() -> true
                HabitFrequency.MIESIECZNY in filters.frequencies &&
                        habit.monthlyDates.isNotEmpty() -> true
                else -> false
            }
        }
    }

    // Ukończone dzisiaj
    if (!filters.showCompleted) {
        val today = java.time.LocalDate.now().toString()
        result = result.filter { !it.completionDates.contains(today) }
    }

    // Minimalny streak
    filters.minStreak?.let { min ->
        result = result.filter { it.streak >= min }
    }

    // Sortowanie
    result = when (filters.sortBy) {
        HabitSortOption.NAME_ASC -> result.sortedBy { it.name.lowercase() }
        HabitSortOption.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
        HabitSortOption.STREAK_HIGH -> result.sortedByDescending { it.streak }
        HabitSortOption.STREAK_LOW -> result.sortedBy { it.streak }
        HabitSortOption.POINTS_HIGH -> result.sortedByDescending { it.points }
        HabitSortOption.POINTS_LOW -> result.sortedBy { it.points }
    }

    return result
}

fun HabitSortOption.toDisplayString(): String {
    return when (this) {
        HabitSortOption.NAME_ASC -> "Nazwa A-Z"
        HabitSortOption.NAME_DESC -> "Nazwa Z-A"
        HabitSortOption.STREAK_HIGH -> "Streak ↓"
        HabitSortOption.STREAK_LOW -> "Streak ↑"
        HabitSortOption.POINTS_HIGH -> "Punkty ↓"
        HabitSortOption.POINTS_LOW -> "Punkty ↑"
    }
}

fun HabitFrequency.toDisplayString(): String {
    return when (this) {
        HabitFrequency.CODZIENNY -> "Codzienny"
        HabitFrequency.TYGODNIOWY -> "Tygodniowy"
        HabitFrequency.MIESIECZNY -> "Miesięczny"
    }
}