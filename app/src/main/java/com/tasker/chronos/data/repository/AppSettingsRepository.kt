// Plik: data/repository/AppSettingsRepository.kt
package com.tasker.chronos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.tasker.chronos.data.models.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppSettingsRepository(private val context: Context) {

    private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
    private val SHOW_TUTORIAL_KEY = booleanPreferencesKey("show_tutorial")

    fun getSettings(): Flow<AppSettings> {
        return context.settingsDataStore.data.map { preferences ->
            AppSettings(
                notificationsEnabled = preferences[NOTIFICATIONS_KEY] ?: true,
                showTutorial = preferences[SHOW_TUTORIAL_KEY] ?: true
            )
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }

    suspend fun setTutorialShown() {
        context.settingsDataStore.edit { preferences ->
            preferences[SHOW_TUTORIAL_KEY] = false
        }
    }
}