// Plik: data/repository/GoalsRepository.kt
package com.tasker.chronos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tasker.chronos.data.models.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.goalsDataStore: DataStore<Preferences> by preferencesDataStore(name = "goals")

class GoalsRepository(private val context: Context) {

    private val GOALS_KEY = stringPreferencesKey("goals_list")
    private val ARCHIVED_GOALS_KEY = stringPreferencesKey("archived_goals_list") // ✅ DODANO
    private val gson = Gson()
    private val goalListType = object : TypeToken<List<Goal>>() {}.type
    private val mutex = Mutex() // ✅ DODANO dla thread-safety

    /**
     * Pobierz wszystkie cele
     */
    fun getAllGoals(): Flow<List<Goal>> {
        return context.goalsDataStore.data.map { preferences ->
            val jsonString = preferences[GOALS_KEY]
            if (jsonString.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    gson.fromJson(jsonString, goalListType)
                } catch (e: Exception) {
                    android.util.Log.e("GoalsRepository", "❌ Błąd deserializacji: ${e.message}")
                    emptyList()
                }
            }
        }
    }

    /**
     * Pobierz aktywne cele (nieukończone)
     */
    fun getActiveGoals(): Flow<List<Goal>> = getAllGoals()
        .map { goals ->
            goals.filter { !it.isCompleted() }
                .also {
                    android.util.Log.d("GoalsRepository", "🔍 Aktywne cele: ${it.size}")
                    it.forEach { goal ->
                        android.util.Log.d("GoalsRepository", "  - ${goal.title} (isCompleted: ${goal.isCompleted()})")
                    }
                }
        }

    // ✅ Archiwalne cele (ukończone)
    fun getArchivedGoals(): Flow<List<Goal>> = getAllGoals()
        .map { goals ->
            goals.filter { it.isCompleted() }
                .also {
                    android.util.Log.d("GoalsRepository", "📦 Cele w archiwum: ${it.size}")
                    it.forEach { goal ->
                        android.util.Log.d("GoalsRepository", "  - ${goal.title}")
                    }
                }
        }

    suspend fun addGoal(goal: Goal) = mutex.withLock {
        context.goalsDataStore.edit { preferences ->
            val jsonString = preferences[GOALS_KEY]
            val currentGoals = if (jsonString.isNullOrBlank()) {
                mutableListOf()
            } else {
                try {
                    gson.fromJson<MutableList<Goal>>(jsonString, goalListType)
                } catch (e: Exception) {
                    android.util.Log.e("GoalsRepository", "❌ Błąd parsowania: ${e.message}")
                    mutableListOf()
                }
            }

            currentGoals.add(goal)
            preferences[GOALS_KEY] = gson.toJson(currentGoals)

            android.util.Log.d("GoalsRepository", "✅ Dodano cel: ${goal.title}")
        }
    }

    /**
     * ✅ ZAKTUALIZOWANO: Automatyczne przenoszenie do archiwum
     */
    suspend fun updateGoal(goal: Goal) = mutex.withLock {
        try {
            context.goalsDataStore.edit { preferences ->
                val currentJsonString = preferences[GOALS_KEY] ?: ""
                val archivedJsonString = preferences[ARCHIVED_GOALS_KEY] ?: ""

                val currentGoals: MutableList<Goal> = if (currentJsonString.isBlank()) {
                    mutableListOf()
                } else {
                    try {
                        gson.fromJson(currentJsonString, goalListType)
                    } catch (e: Exception) {
                        android.util.Log.e("GoalsRepository", "❌ Błąd parsowania current: ${e.message}")
                        mutableListOf()
                    }
                }

                val archivedGoals: MutableList<Goal> = if (archivedJsonString.isBlank()) {
                    mutableListOf()
                } else {
                    try {
                        gson.fromJson(archivedJsonString, goalListType)
                    } catch (e: Exception) {
                        android.util.Log.e("GoalsRepository", "❌ Błąd parsowania archived: ${e.message}")
                        mutableListOf()
                    }
                }

                val indexInCurrent = currentGoals.indexOfFirst { it.id == goal.id }
                val indexInArchived = archivedGoals.indexOfFirst { it.id == goal.id }

                when {
                    indexInCurrent != -1 -> {
                        if (goal.isCompleted()) {
                            // ✅ Cel ukończony w 100% - przenieś do archiwum
                            currentGoals.removeAt(indexInCurrent)
                            archivedGoals.add(0, goal)
                            android.util.Log.d("GoalsRepository", "📦 Przeniesiono do archiwum: ${goal.title}")
                        } else {
                            currentGoals[indexInCurrent] = goal
                            android.util.Log.d("GoalsRepository", "✏️ Zaktualizowano: ${goal.title}")
                        }
                    }

                    indexInArchived != -1 -> {
                        if (!goal.isCompleted()) {
                            // ↩️ Przywróć z archiwum
                            archivedGoals.removeAt(indexInArchived)
                            currentGoals.add(goal)
                            android.util.Log.d("GoalsRepository", "↩️ Przywrócono: ${goal.title}")
                        } else {
                            archivedGoals[indexInArchived] = goal
                            android.util.Log.d("GoalsRepository", "✏️ Zaktualizowano w archiwum: ${goal.title}")
                        }
                    }

                    else -> {
                        android.util.Log.w("GoalsRepository", "⚠️ Cel nie znaleziony, dodaję jako nowy")
                        if (goal.isCompleted()) {
                            archivedGoals.add(0, goal)
                        } else {
                            currentGoals.add(goal)
                        }
                    }
                }

                preferences[GOALS_KEY] = gson.toJson(currentGoals)
                preferences[ARCHIVED_GOALS_KEY] = gson.toJson(archivedGoals)
            }
        } catch (e: Exception) {
            android.util.Log.e("GoalsRepository", "❌ Błąd updateGoal: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteGoal(goalId: String) = mutex.withLock {
        context.goalsDataStore.edit { preferences ->
            val currentJsonString = preferences[GOALS_KEY]
            val archivedJsonString = preferences[ARCHIVED_GOALS_KEY]

            if (currentJsonString != null) {
                val currentGoals: MutableList<Goal> = try {
                    gson.fromJson(currentJsonString, goalListType)
                } catch (e: Exception) {
                    android.util.Log.e("GoalsRepository", "❌ Błąd parsowania: ${e.message}")
                    mutableListOf()
                }

                val wasRemoved = currentGoals.removeAll { it.id == goalId }
                if (wasRemoved) {
                    preferences[GOALS_KEY] = gson.toJson(currentGoals)
                    android.util.Log.d("GoalsRepository", "🗑️ Usunięto z aktywnych")
                }
            }

            if (archivedJsonString != null) {
                val archivedGoals: MutableList<Goal> = try {
                    gson.fromJson(archivedJsonString, goalListType)
                } catch (e: Exception) {
                    android.util.Log.e("GoalsRepository", "❌ Błąd parsowania: ${e.message}")
                    mutableListOf()
                }

                val wasRemoved = archivedGoals.removeAll { it.id == goalId }
                if (wasRemoved) {
                    preferences[ARCHIVED_GOALS_KEY] = gson.toJson(archivedGoals)
                    android.util.Log.d("GoalsRepository", "🗑️ Usunięto z archiwum")
                }
            }
        }
    }

    /**
     * ✅ NOWE: Wyczyść archiwum celów
     */
    suspend fun clearArchive() = mutex.withLock {
        context.goalsDataStore.edit { preferences ->
            preferences.remove(ARCHIVED_GOALS_KEY)
            android.util.Log.d("GoalsRepository", "🗑️ Wyczyszczono archiwum celów")
        }
    }
}