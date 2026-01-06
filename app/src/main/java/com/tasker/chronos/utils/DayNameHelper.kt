// Plik: utils/DayNameHelper.kt
package com.tasker.chronos.utils

import java.time.DayOfWeek

object DayNameHelper {

    // Polskie nazwy dni (standardowe dla UI)
    val POLISH_DAYS = listOf(
        "Poniedziałek",
        "Wtorek",
        "Środa",
        "Czwartek",
        "Piątek",
        "Sobota",
        "Niedziela"
    )

    // Krótkie wersje
    val POLISH_DAYS_SHORT = listOf(
        "Pon",
        "Wt",
        "Śr",
        "Czw",
        "Pt",
        "Sob",
        "Niedz"
    )

    /**
     * Konwertuj nazwę dnia na DayOfWeek (1=Monday, 7=Sunday)
     */
    fun toDayOfWeekValue(dayName: String): Int {
        return when (dayName.lowercase().trim()) {
            // Polski
            "poniedziałek", "pon", "poniedzialek" -> 1
            "wtorek", "wt" -> 2
            "środa", "śr", "sr", "sroda" -> 3
            "czwartek", "czw" -> 4
            "piątek", "pt", "piatek" -> 5
            "sobota", "sob", "sb" -> 6
            "niedziela", "niedz", "nd" -> 7

            // Angielski (fallback)
            "monday", "mon" -> 1
            "tuesday", "tue" -> 2
            "wednesday", "wed" -> 3
            "thursday", "thu" -> 4
            "friday", "fri" -> 5
            "saturday", "sat" -> 6
            "sunday", "sun" -> 7

            else -> {
                android.util.Log.w("DayNameHelper", "⚠️ Nieznany dzień: $dayName")
                1 // Default: Poniedziałek
            }
        }
    }

    /**
     * Konwertuj DayOfWeek na polską nazwę
     */
    fun toPolishName(dayOfWeek: DayOfWeek): String {
        return POLISH_DAYS[dayOfWeek.value - 1]
    }

    /**
     * Konwertuj DayOfWeek na krótką polską nazwę
     */
    fun toPolishShortName(dayOfWeek: DayOfWeek): String {
        return POLISH_DAYS_SHORT[dayOfWeek.value - 1]
    }

    /**
     * Sprawdź czy string jest poprawną nazwą dnia
     */
    fun isValidDayName(dayName: String): Boolean {
        return toDayOfWeekValue(dayName) in 1..7
    }

    /**
     * Normalizuj nazwę dnia do standardowej formy (Poniedziałek, Wtorek, etc.)
     */
    fun normalize(dayName: String): String {
        val value = toDayOfWeekValue(dayName)
        return POLISH_DAYS[value - 1]
    }
}