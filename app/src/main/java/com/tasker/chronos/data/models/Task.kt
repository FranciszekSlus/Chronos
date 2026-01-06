// Plik: data/models/Task.kt
package com.tasker.chronos.data.models

import java.util.UUID

/**
 * Priorytet zadania
 */
enum class TaskPriority {
    LOW,      // Niski - zielony
    MEDIUM,   // Średni - żółty
    HIGH      // Wysoki - czerwony
}

/**
 * Typ przypomnienia
 */
enum class ReminderType {
    MINUTES_BEFORE,  // X minut przed (domyślne)
    CUSTOM_TIME,     // O konkretnej godzinie w dniu zadania
    DAYS_BEFORE      // X dni przed o określonej godzinie
}

/**
 * Zadanie do wykonania
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val date: String? = null,
    val time: String? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val hasReminder: Boolean = false,
    val reminderMinutesBefore: Int = 60,
    // ✅ DOMYŚLNA WARTOŚĆ zamiast nullable - naprawia stare zadania
    val reminderType: ReminderType = ReminderType.MINUTES_BEFORE,
    val reminderCustomTime: String? = null,
    val reminderCustomDays: Int? = null,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val createdAt: String = java.time.LocalDateTime.now().toString()
)

/**
 * Pomocnicza funkcja do pobierania koloru priorytetu
 */
fun TaskPriority.toColor(): androidx.compose.ui.graphics.Color {
    return when (this) {
        TaskPriority.LOW -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        TaskPriority.MEDIUM -> androidx.compose.ui.graphics.Color(0xFFFFC107)
        TaskPriority.HIGH -> androidx.compose.ui.graphics.Color(0xFFF44336)
    }
}

fun TaskPriority.toDisplayName(): String {
    return when (this) {
        TaskPriority.LOW -> "Niski"
        TaskPriority.MEDIUM -> "Średni"
        TaskPriority.HIGH -> "Wysoki"
    }
}

/**
 * Helper do formatowania przypomnienia
 */
fun Task.getReminderDescription(): String {
    if (!hasReminder) return "Brak"

    return when (reminderType) {
        ReminderType.MINUTES_BEFORE -> {
            when (reminderMinutesBefore) {
                5 -> "5 minut przed"
                15 -> "15 minut przed"
                30 -> "30 minut przed"
                60 -> "1 godzinę przed"
                1440 -> "1 dzień przed"
                else -> "$reminderMinutesBefore minut przed"
            }
        }
        ReminderType.CUSTOM_TIME -> {
            "O godzinie ${reminderCustomTime ?: "??"}"
        }
        ReminderType.DAYS_BEFORE -> {
            val days = reminderCustomDays ?: 1
            val time = reminderCustomTime ?: "09:00"
            "$days dni przed o $time"
        }
    }
}