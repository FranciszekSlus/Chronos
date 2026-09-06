package com.tasker.chronos.utils

import java.time.LocalDate

/**
 * Material3 DatePicker uses UTC midnight millis for calendar dates.
 * Convert via epoch-day so local timezone never shifts the day by ±1.
 */
object DateMillis {
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun localDateToUtcMillis(date: LocalDate): Long = date.toEpochDay() * DAY_MS

    fun utcMillisToLocalDate(millis: Long): LocalDate =
        LocalDate.ofEpochDay(millis / DAY_MS)

    fun parseToUtcMillisOrNow(date: String?): Long {
        return try {
            if (date.isNullOrBlank()) System.currentTimeMillis()
            else localDateToUtcMillis(LocalDate.parse(date))
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
