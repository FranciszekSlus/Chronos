// Plik: data/models/Habit.kt
package com.tasker.chronos.data.models

import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val completionDates: List<String> = emptyList(), // Format: "2025-11-02"
    val weeklyDays: List<String> = emptyList(), // Dni tygodnia: ["MONDAY", "WEDNESDAY"]
    val monthlyDates: List<String> = emptyList(), // Dni miesiąca: ["1", "15", "30"]
    val hasReminder: Boolean = false,
    val reminderTime: String? = null, // Format: "HH:mm"
    val streak: Int = 0, // Aktualny streak
    val lastCompletionDate: String? = null, // Ostatnia data wykonania (yyyy-MM-dd)
    val points: Int = 0 // NOWE: Suma punktów zdobytych za ten nawyk
)