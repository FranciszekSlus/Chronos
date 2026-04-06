// Plik: data/models/UserProfile.kt
package com.tasker.chronos.data.models

data class UserProfile(
    val totalPoints: Int = 0,
    val createdAt: String = "",
    val unlockedAchievements: List<String> = emptyList() // ✅ IDs odblokowanych osiągnięć
)