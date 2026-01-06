package com.tasker.chronos.data.models


data class TaskReminder(
    val value: Int,                  // np. 30, 1, 2
    val unit: TaskReminderUnit      // ZMIANA: TaskReminderUnit zamiast ReminderUnit
) {
    /**
     * Zwraca tekst do wyświetlenia, np. "30 minut przed"
     */
    fun getDisplayText(): String {
        return "$value ${unit.getPolishName(value)}"
    }

    /**
     * Oblicza ile minut przed wydarzeniem ma być przypomnienie
     */
    fun getTotalMinutes(): Long {
        return when (unit) {
            TaskReminderUnit.MINUTES -> value.toLong()
            TaskReminderUnit.HOURS -> value * 60L
            TaskReminderUnit.DAYS -> value * 24 * 60L
            TaskReminderUnit.WEEKS -> value * 7 * 24 * 60L
        }
    }
}

/**
 * Jednostki czasu dla przypomnień ZADAŃ (nie mylić z ReminderUnit dla celów)
 */
enum class TaskReminderUnit(val displayName: String) {
    MINUTES("minuty"),
    HOURS("godziny"),
    DAYS("dni"),
    WEEKS("tygodnie");

    /**
     * Zwraca poprawną formę polską w zależności od liczby
     */
    fun getPolishName(value: Int): String {
        return when (this) {
            MINUTES -> when {
                value == 1 -> "minuta"
                value % 10 in 2..4 && value % 100 !in 12..14 -> "minuty"
                else -> "minut"
            }
            HOURS -> when {
                value == 1 -> "godzina"
                value % 10 in 2..4 && value % 100 !in 12..14 -> "godziny"
                else -> "godzin"
            }
            DAYS -> when {
                value == 1 -> "dzień"
                else -> "dni"
            }
            WEEKS -> when {
                value == 1 -> "tydzień"
                value % 10 in 2..4 && value % 100 !in 12..14 -> "tygodnie"
                else -> "tygodni"
            }
        }
    }
}

