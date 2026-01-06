// Plik: data/models/GoalFilters.kt
package com.tasker.chronos.data.models

data class GoalFilters(
    val searchQuery: String = "",
    val progressRange: ProgressRange? = null, // 0-25%, 25-50%, 50-75%, 75-100%
    val showCompleted: Boolean = true,
    val sortBy: GoalSortOption = GoalSortOption.DATE_ASC
)

enum class ProgressRange {
    RANGE_0_25,     // 0-25%
    RANGE_25_50,    // 25-50%
    RANGE_50_75,    // 50-75%
    RANGE_75_100    // 75-100%
}

enum class GoalSortOption {
    NAME_ASC,       // A-Z
    NAME_DESC,      // Z-A
    DATE_ASC,       // Data końcowa ↑
    DATE_DESC,      // Data końcowa ↓
    PROGRESS_HIGH,  // Postęp ↓
    PROGRESS_LOW    // Postęp ↑
}

fun List<Goal>.applyFilters(filters: GoalFilters): List<Goal> {
    var result = this

    // Wyszukiwanie
    if (filters.searchQuery.isNotBlank()) {
        result = result.filter {
            it.title.contains(filters.searchQuery, ignoreCase = true) ||
                    it.notes.contains(filters.searchQuery, ignoreCase = true)
        }
    }

    // Zakres postępu
    filters.progressRange?.let { range ->
        result = result.filter { goal ->
            val progress = goal.getProgressPercentage() // ✅ Użyj funkcji
            when (range) {
                ProgressRange.RANGE_0_25 -> progress in 0..25
                ProgressRange.RANGE_25_50 -> progress in 26..50
                ProgressRange.RANGE_50_75 -> progress in 51..75
                ProgressRange.RANGE_75_100 -> progress in 76..100
            }
        }
    }

    // Ukończone
    // Ukończone
    if (!filters.showCompleted) {
        result = result.filter { !it.isCompleted() } // ✅ Użyj funkcji ()
    }

    // Sortowanie
    result = when (filters.sortBy) {
        GoalSortOption.NAME_ASC -> result.sortedBy { it.title.lowercase() }
        GoalSortOption.NAME_DESC -> result.sortedByDescending { it.title.lowercase() }
        GoalSortOption.DATE_ASC -> result.sortedBy { it.endDate }
        GoalSortOption.DATE_DESC -> result.sortedByDescending { it.endDate }
        GoalSortOption.PROGRESS_HIGH -> result.sortedByDescending { it.getProgressPercentage() } // ✅
        GoalSortOption.PROGRESS_LOW -> result.sortedBy { it.getProgressPercentage() } // ✅
    }

    return result
}

fun GoalSortOption.toDisplayString(): String {
    return when (this) {
        GoalSortOption.NAME_ASC -> "Nazwa A-Z"
        GoalSortOption.NAME_DESC -> "Nazwa Z-A"
        GoalSortOption.DATE_ASC -> "Termin ↑"
        GoalSortOption.DATE_DESC -> "Termin ↓"
        GoalSortOption.PROGRESS_HIGH -> "Postęp ↓"
        GoalSortOption.PROGRESS_LOW -> "Postęp ↑"
    }
}

fun ProgressRange.toDisplayString(): String {
    return when (this) {
        ProgressRange.RANGE_0_25 -> "0-25%"
        ProgressRange.RANGE_25_50 -> "25-50%"
        ProgressRange.RANGE_50_75 -> "50-75%"
        ProgressRange.RANGE_75_100 -> "75-100%"
    }
}