// Plik: data/models/CustomEvent.kt - ZAKTUALIZOWANY
package com.tasker.chronos.data.models

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class CustomEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val date: String,              // Format: "2025-01-01" (data rozpoczęcia)
    val endDate: String? = null,   // Data zakończenia (opcjonalna)
    val startTime: String,         // Format: "18:00"
    val endTime: String,           // Format: "10:00"
    val color: Long = 0xFF2196F3,  // Kolor wydarzenia
    val hasReminder: Boolean = false,
    val reminderMinutesBefore: Int = 15,
    val createdAt: String = java.time.LocalDateTime.now().toString(),
    val sourceTaskId: String? = null,
    val sourceHabitId: String? = null,
    val sourceGoalId: String? = null,      // ✅ DODAJ TO
    val sourceMiniGoalId: String? = null,
    val showInMonthView: Boolean = true , // ✅ DODAJ TO
) {

    /**
     * Sprawdza czy wydarzenie obejmuje dany dzień
     */
    fun occursOnDate(dateString: String): Boolean {
        return try {
            val checkDate = LocalDate.parse(dateString)
            val startDate = LocalDate.parse(date)
            val finalEndDate = endDate?.let { LocalDate.parse(it) } ?: startDate

            !checkDate.isBefore(startDate) && !checkDate.isAfter(finalEndDate)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Zwraca listę wszystkich dni, które obejmuje wydarzenie
     */
    fun getAllDates(): List<String> {
        return try {
            val startDate = LocalDate.parse(date)
            val finalEndDate = endDate?.let { LocalDate.parse(it) } ?: startDate

            val dates = mutableListOf<String>()
            var currentDate = startDate

            while (!currentDate.isAfter(finalEndDate)) {
                dates.add(currentDate.toString())
                currentDate = currentDate.plusDays(1)
            }

            dates
        } catch (e: Exception) {
            listOf(date)
        }
    }

    /**
     * ✅ NOWA FUNKCJA: Zwraca godzinę początkową dla danego dnia
     * - Pierwszy dzień: startTime
     * - Środkowe dni: "00:00"
     * - Nie używane dla ostatniego dnia (bo to getEndTimeForDate)
     */
    fun getStartTimeForDate(dateString: String): String {
        return try {
            val checkDate = LocalDate.parse(dateString)
            val startDate = LocalDate.parse(date)

            if (checkDate == startDate) {
                startTime  // Pierwszy dzień - użyj oryginalnej godziny startu
            } else {
                "00:00"    // Środkowe/ostatnie dni - zacznij od północy
            }
        } catch (e: Exception) {
            startTime
        }
    }

    /**
     * ✅ NOWA FUNKCJA: Zwraca godzinę końcową dla danego dnia
     * - Ostatni dzień: endTime
     * - Wcześniejsze dni: "23:59"
     */
    fun getEndTimeForDate(dateString: String): String {
        return try {
            val checkDate = LocalDate.parse(dateString)
            val finalEndDate = endDate?.let { LocalDate.parse(it) } ?: LocalDate.parse(date)

            if (checkDate == finalEndDate) {
                endTime    // Ostatni dzień - użyj oryginalnej godziny końca
            } else {
                "23:59"    // Wcześniejsze dni - trwaj do końca dnia
            }
        } catch (e: Exception) {
            endTime
        }
    }

    /**
     * ✅ NOWA FUNKCJA: Czy to wielodniowe wydarzenie?
     */
    fun isMultiDay(): Boolean {
        return endDate != null && endDate != date
    }
}