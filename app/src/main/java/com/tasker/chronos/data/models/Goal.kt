// Plik: data/models/Goal.kt
package com.tasker.chronos.data.models

import java.util.UUID

/**
 * Mini-cel - mniejszy krok do osiągnięcia głównego celu
 */
data class MiniGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String? = null,       // Opcjonalna data (jednorazowe)
    val isCompleted: Boolean = false,  // Dla mini-celów Z DATĄ
    val completedAt: String? = null,
    val dailyCompletions: List<String> = emptyList()  // ✅ NOWE: Daty ukończenia (dla BEZ daty)
) {
    /**
     * ✅ Sprawdź czy mini-cel jest ukończony dzisiaj (dla BEZ daty)
     */
    /**
     * ✅ Sprawdź czy mini-cel jest ukończony dzisiaj (dla BEZ daty)
     */
    fun isCompletedToday(): Boolean {
        if (date != null) return isCompleted  // Mini-cele z datą - użyj isCompleted

        val today = java.time.LocalDate.now().toString()
        return dailyCompletions?.contains(today) ?: false  // ✅ Zabezpieczenie przed null
    }

    /**
     * ✅ Czy mini-cel jest "ostatecznie ukończony" (dla postępu celu)
     */
    fun isFinallyCompleted(): Boolean {
        return if (date != null) {
            isCompleted  // Mini-cele z datą - sprawdź isCompleted
        } else {
            false  // Mini-cele bez daty nigdy nie są "ukończone" (są codzienne)
        }
    }
}

/**
 * Główny cel z mini-celami i notatkami
 */
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val endDate: String? = null,
    val color: Long = 0xFF2196F3L,  // ✅ Typ Long, nie String
    val miniGoals: List<MiniGoal> = emptyList(),
    val notes: String = "",
    val createdAt: String = java.time.LocalDateTime.now().toString()
) {
    /**
     * Oblicza procent wykonania na podstawie ukończonych mini-celów (0.0 - 1.0)
     */
    fun getProgress(): Float {
        if (miniGoals.isEmpty()) return 0f

        // ✅ Licznik uwzględnia WSZYSTKIE typy mini-celów
        val completed = miniGoals.count { miniGoal ->
            if (miniGoal.date != null) {
                miniGoal.isFinallyCompleted()  // Mini-cele z datą
            } else {
                miniGoal.isCompletedToday()    // Mini-cele bez daty (dzisiejszy status)
            }
        }

        return (completed.toFloat() / miniGoals.size.toFloat())
    }

    fun getProgressPercentage(): Int {
        if (miniGoals.isEmpty()) return 0

        // ✅ Licznik uwzględnia WSZYSTKIE typy mini-celów
        val completed = miniGoals.count { miniGoal ->
            if (miniGoal.date != null) {
                miniGoal.isFinallyCompleted()
            } else {
                miniGoal.isCompletedToday()
            }
        }

        return (completed * 100 / miniGoals.size)
    }

    fun isCompleted(): Boolean {
        if (miniGoals.isEmpty()) return false

        // ✅ Cel jest ukończony tylko jeśli WSZYSTKIE mini-cele z datami są ukończone
        // Mini-cele bez daty (codzienne) NIE liczą się do "ostatecznego" ukończenia
        val miniGoalsWithDate = miniGoals.filter { it.date != null }

        return miniGoalsWithDate.isNotEmpty() &&
                miniGoalsWithDate.all { it.isFinallyCompleted() }
    }
}