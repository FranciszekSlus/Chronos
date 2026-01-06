package com.tasker.chronos.data.models

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val showTutorial: Boolean = true // Czy pokazać tutorial przy pierwszym uruchomieniu
)