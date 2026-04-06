package com.tasker.chronos.data.models

import java.util.UUID

data class ScheduleEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startTime: String,   // "09:00"
    val endTime: String,     // "10:00"
    val color: Long = 0xFF2196F3L,
    val description: String = ""
)

data class DaySchedule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val events: List<ScheduleEvent> = emptyList(),
    val createdAt: String = java.time.LocalDateTime.now().toString()
)