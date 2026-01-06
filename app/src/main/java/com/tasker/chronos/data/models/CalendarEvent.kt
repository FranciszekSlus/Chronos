// Plik: data/models/CalendarEvent.kt
package com.tasker.chronos.data.models

import java.util.UUID

/**
 * Typ wydarzenia w kalendarzu
 */
enum class EventType {
    HABIT,      // Nawyk (zielony)
    TASK,       // Zadanie (niebieski)
    GOAL,       // Cel/Mini-cel (pomarańczowy)
    CUSTOM      // Własne wydarzenie (szary)
}

/**
 * Wydarzenie wyświetlane w kalendarzu
 */
data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String,           // Format: "2025-11-02"
    val startTime: String? = null,  // Format: "09:00" (opcjonalne)
    val endTime: String? = null,    // Format: "10:00" (opcjonalne)
    val type: EventType,
    val sourceId: String? = null,   // ID źródłowego obiektu (Habit/Task/Goal)
    val isCompleted: Boolean = false,
    val hasReminder: Boolean = false,
    val reminderTime: String? = null,
    val sourceTaskId: String? = null,  // ✅ DODAJ TO
    val sourceHabitId: String? = null

)

/**
 * Pomocnicza klasa do grupowania wydarzeń według dnia
 */
data class DayEvents(
    val date: String,
    val events: List<CalendarEvent>
)