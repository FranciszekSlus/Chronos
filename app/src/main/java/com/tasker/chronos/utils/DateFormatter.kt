package com.tasker.chronos.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun formatDaysLeft(targetDate: LocalDate?): String? {
    targetDate ?: return null

    val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), targetDate)

    if (daysLeft < 0) return null // Termin minął

    return when {
        daysLeft >= 365 -> {
            val years = daysLeft / 365
            val remainingDays = daysLeft % 365
            if (remainingDays > 0) "$years lat, $remainingDays dni" else "$years lat"
        }
        daysLeft >= 31 -> {
            val months = daysLeft / 31
            val remainingDays = daysLeft % 31
            if (remainingDays > 0) "$months mies., $remainingDays dni" else "$months mies."
        }
        else -> "$daysLeft dni"
    }
}