// Plik: data/models/Goal.kt
package com.tasker.chronos.data.models

import java.util.UUID

/**
 * Mini-cel - mniejszy krok do osiągnięcia głównego celu
 */
data class MiniGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String? = null,       // null = "bez daty", można dodawać codziennie do kalendarza
    val isCompleted: Boolean = false,  // ✅ DLA WSZYSTKICH mini-celów
    val completedAt: String? = null
    // ❌ USUŃ: val dailyCompletions: List<String> = emptyList()
) {
    /**
     * ✅ Czy mini-cel jest ukończony (dla WSZYSTKICH typów)
     */
    fun isFinallyCompleted(): Boolean {
        return isCompleted  // ✅ Prosto - sprawdź flagę
    }

    // ❌ USUŃ metodę isCompletedToday() - nie jest już potrzebna
}

/**
 * Główny cel z mini-celami i notatkami
 */
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val endDate: String? = null,
    val color: Long = 0xFF2196F3L,
    val miniGoals: List<MiniGoal> = emptyList(),
    val notes: String = "",
    val createdAt: String = java.time.LocalDateTime.now().toString(),
    val manuallyCompleted: Boolean = false  // ✅ NOWE: Oznacz ręcznie jako ukończony
) {
    /**
     * Oblicza procent wykonania na podstawie ukończonych mini-celów (0.0 - 1.0)
     */
    fun getProgress(): Float {
        if (miniGoals.isEmpty()) return 0f

        // ✅ Policz ukończone mini-cele (niezależnie od daty)
        val completed = miniGoals.count { it.isCompleted }

        return (completed.toFloat() / miniGoals.size.toFloat())
    }

    fun getProgressPercentage(): Int {
        if (miniGoals.isEmpty()) return 0

        // ✅ Policz ukończone mini-cele (niezależnie od daty)
        val completed = miniGoals.count { it.isCompleted }

        return (completed * 100 / miniGoals.size)
    }

    fun isCompleted(): Boolean {
        android.util.Log.d("Goal", "========================================")
        android.util.Log.d("Goal", "🔍 Sprawdzanie isCompleted() dla: $title")
        android.util.Log.d("Goal", "📊 Wszystkie mini-cele: ${miniGoals.size}")

        if (miniGoals.isEmpty()) {
            android.util.Log.d("Goal", "❌ Brak mini-celów - cel nieukończony")
            return false
        }

        // ✅ NOWA PROSTA LOGIKA: Sprawdź czy WSZYSTKIE mini-cele mają isCompleted = true
        miniGoals.forEach { mini ->
            android.util.Log.d("Goal", "  - '${mini.title}' | date: ${mini.date ?: "BRAK (można dodawać codziennie)"} | isCompleted: ${mini.isCompleted}")
        }

        val allCompleted = miniGoals.all { it.isCompleted }

        android.util.Log.d("Goal", "🎯 Wszystkie mini-cele ukończone: $allCompleted")
        android.util.Log.d("Goal", "========================================")

        return allCompleted
    }
}