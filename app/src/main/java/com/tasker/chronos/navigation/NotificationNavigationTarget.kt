package com.tasker.chronos.navigation

/**
 * Target opened when the user taps a reminder notification.
 * Prefer tab navigation only — entity IDs are kept for optional scroll/highlight, not auto-edit.
 */
data class NotificationNavigationTarget(
    val tabIndex: Int = 0,
    val taskId: String? = null,
    val habitId: String? = null,
    val goalId: String? = null,
    val openCalendar: Boolean = false,
    val calendarEventId: String? = null,
    val calendarEventDate: String? = null,
    val nonce: Long = System.currentTimeMillis()
)
