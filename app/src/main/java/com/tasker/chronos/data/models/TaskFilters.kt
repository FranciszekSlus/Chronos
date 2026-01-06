// Plik: data/models/TaskFilters.kt
package com.tasker.chronos.data.models

/**
 * Filtry zadań
 */
data class TaskFilters(
    val searchQuery: String = "",
    val priorities: Set<TaskPriority> = emptySet(),
    val dateRange: DateRange? = null,
    val sortBy: TaskSortOption = TaskSortOption.DATE_ASC
)

enum class TaskSortOption {
    DATE_ASC,        // Od najstarszych
    DATE_DESC,       // Od najnowszych
    PRIORITY_HIGH,   // Wysokie priorytety najpierw
    PRIORITY_LOW,    // Niskie priorytety najpierw
    NAME_ASC,        // Alfabetycznie A-Z
    NAME_DESC        // Alfabetycznie Z-A
}

data class DateRange(
    val startDate: String?,  // Format: "2025-01-15"
    val endDate: String?
)

/**
 * Rozszerzenie do filtrowania listy zadań
 */
fun List<Task>.applyFilters(filters: TaskFilters): List<Task> {
    var result = this

    // Filtr wyszukiwania (nazwa)
    if (filters.searchQuery.isNotBlank()) {
        result = result.filter { task ->
            task.title.contains(filters.searchQuery, ignoreCase = true) ||
                    task.description.contains(filters.searchQuery, ignoreCase = true)
        }
    }

    // Filtr priorytetów
    if (filters.priorities.isNotEmpty()) {
        result = result.filter { it.priority in filters.priorities }
    }

    // Filtr zakresu dat
    filters.dateRange?.let { range ->
        result = result.filter { task ->
            if (task.date == null) return@filter false

            val taskDate = task.date
            val inRange = when {
                range.startDate != null && range.endDate != null ->
                    taskDate >= range.startDate && taskDate <= range.endDate
                range.startDate != null ->
                    taskDate >= range.startDate
                range.endDate != null ->
                    taskDate <= range.endDate
                else -> true
            }
            inRange
        }
    }

    // Sortowanie
    result = when (filters.sortBy) {
        TaskSortOption.DATE_ASC -> result.sortedBy { it.date ?: "9999-12-31" }
        TaskSortOption.DATE_DESC -> result.sortedByDescending { it.date ?: "0000-01-01" }
        TaskSortOption.PRIORITY_HIGH -> result.sortedByDescending { it.priority.ordinal }
        TaskSortOption.PRIORITY_LOW -> result.sortedBy { it.priority.ordinal }
        TaskSortOption.NAME_ASC -> result.sortedBy { it.title.lowercase() }
        TaskSortOption.NAME_DESC -> result.sortedByDescending { it.title.lowercase() }
    }

    return result
}

/**
 * Pomocnicze funkcje
 */
fun TaskSortOption.toDisplayString(): String {
    return when (this) {
        TaskSortOption.DATE_ASC -> "Data ↑"
        TaskSortOption.DATE_DESC -> "Data ↓"
        TaskSortOption.PRIORITY_HIGH -> "Priorytet ↓"
        TaskSortOption.PRIORITY_LOW -> "Priorytet ↑"
        TaskSortOption.NAME_ASC -> "Nazwa A-Z"
        TaskSortOption.NAME_DESC -> "Nazwa Z-A"
    }
}

fun TaskPriority.toDisplayString(): String {
    return when (this) {
        TaskPriority.LOW -> "Niski"
        TaskPriority.MEDIUM -> "Średni"
        TaskPriority.HIGH -> "Wysoki"
    }
}