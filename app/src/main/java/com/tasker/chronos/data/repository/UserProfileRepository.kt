// Plik: data/repository/UserProfileRepository.kt
package com.tasker.chronos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tasker.chronos.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserProfileRepository(private val context: Context) {

    private val TOTAL_POINTS_KEY = intPreferencesKey("total_points")
    private val CREATED_AT_KEY = stringPreferencesKey("created_at")



    suspend fun addPoints(points: Int) {
        context.userDataStore.edit { preferences ->
            val currentPoints = preferences[TOTAL_POINTS_KEY] ?: 0
            preferences[TOTAL_POINTS_KEY] = currentPoints + points

            // Ustaw datę utworzenia jeśli nie istnieje
            if (preferences[CREATED_AT_KEY] == null) {
                preferences[CREATED_AT_KEY] = LocalDate.now().toString()
            }
        }
    }

    suspend fun resetPoints() {
        context.userDataStore.edit { preferences ->
            preferences[TOTAL_POINTS_KEY] = 0
        }
    }
    private val UNLOCKED_ACHIEVEMENTS_KEY = stringPreferencesKey("unlocked_achievements")

    fun getUserProfile(): Flow<UserProfile> {
        return context.userDataStore.data.map { preferences ->
            UserProfile(
                totalPoints = preferences[TOTAL_POINTS_KEY] ?: 0,
                createdAt = preferences[CREATED_AT_KEY] ?: LocalDate.now().toString(),
                unlockedAchievements = preferences[UNLOCKED_ACHIEVEMENTS_KEY]
                    ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()  // ✅ NOWE
            )
        }
    }

    suspend fun unlockAchievement(achievementId: String) {
        context.userDataStore.edit { preferences ->
            val current = preferences[UNLOCKED_ACHIEVEMENTS_KEY] ?: ""
            val list = current.split(",").filter { it.isNotBlank() }.toMutableList()
            if (!list.contains(achievementId)) {
                list.add(achievementId)
                preferences[UNLOCKED_ACHIEVEMENTS_KEY] = list.joinToString(",")
            }
        }
    }
}