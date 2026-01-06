// Plik: data/repository/HabitsRepository.kt
package com.tasker.chronos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tasker.chronos.data.models.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.habitsDataStore: DataStore<Preferences> by preferencesDataStore(name = "habits")

class HabitsRepository(private val context: Context) {

    private val HABITS_KEY = stringPreferencesKey("habits_list")
    private val gson = Gson()

    private val habitListType = object : TypeToken<List<Habit>>() {}.type

    /**
     * Pobierz wszystkie nawyki (Flow)
     */
    fun getAllHabits(): Flow<List<Habit>> {
        return context.habitsDataStore.data.map { preferences ->
            val jsonString = preferences[HABITS_KEY]
            if (jsonString.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    gson.fromJson(jsonString, habitListType)
                } catch (e: Exception) {
                    android.util.Log.e("HabitsRepository", "❌ Błąd deserializacji: ${e.message}")
                    emptyList()
                }
            }
        }
    }

    /**
     * POPRAWKA: Pobierz wszystkie nawyki jako listę (dla Worker/Scheduler)
     */
    suspend fun getAllHabitsAsList(): List<Habit> {
        return try {
            val preferences = context.habitsDataStore.data.first()
            val jsonString = preferences[HABITS_KEY]

            if (jsonString.isNullOrBlank()) {
                emptyList()
            } else {
                gson.fromJson(jsonString, habitListType)
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitsRepository", "❌ Błąd pobierania listy: ${e.message}")
            emptyList()
        }
    }

    suspend fun addHabit(habit: Habit) {
        context.habitsDataStore.edit { preferences ->
            val jsonString = preferences[HABITS_KEY]
            val currentHabits = if (jsonString.isNullOrBlank()) {
                mutableListOf()
            } else {
                gson.fromJson<MutableList<Habit>>(jsonString, habitListType)
            }
            currentHabits.add(habit)
            preferences[HABITS_KEY] = gson.toJson(currentHabits)

            android.util.Log.d("HabitsRepository", "➕ Dodano nawyk: ${habit.name}")
        }
    }

    suspend fun updateHabit(habit: Habit) {
        context.habitsDataStore.edit { preferences ->
            val jsonString = preferences[HABITS_KEY]
            if (jsonString.isNullOrBlank()) {
                android.util.Log.w("HabitsRepository", "⚠️ Brak nawyków do aktualizacji")
                return@edit
            }

            val currentHabits: MutableList<Habit> = gson.fromJson(jsonString, habitListType)
            val index = currentHabits.indexOfFirst { it.id == habit.id }

            if (index != -1) {
                currentHabits[index] = habit
                preferences[HABITS_KEY] = gson.toJson(currentHabits)
                android.util.Log.d("HabitsRepository", "✏️ Zaktualizowano nawyk: ${habit.name}")
            } else {
                // Nawyk nie istnieje - dodaj
                currentHabits.add(habit)
                preferences[HABITS_KEY] = gson.toJson(currentHabits)
                android.util.Log.d("HabitsRepository", "➕ Nowy nawyk: ${habit.name}")
            }
        }
    }

    suspend fun updateHabits(habitsToUpdate: List<Habit>) {
        context.habitsDataStore.edit { preferences ->
            val jsonString = preferences[HABITS_KEY]
            if (jsonString.isNullOrBlank()) return@edit

            val currentHabits: MutableList<Habit> = gson.fromJson(jsonString, habitListType)
            val updateMap = habitsToUpdate.associateBy { it.id }

            val updatedList = currentHabits.map { habit ->
                updateMap[habit.id] ?: habit
            }

            preferences[HABITS_KEY] = gson.toJson(updatedList)
            android.util.Log.d("HabitsRepository", "✏️ Zaktualizowano ${habitsToUpdate.size} nawyków")
        }
    }

    suspend fun deleteHabit(habitId: String) {
        context.habitsDataStore.edit { preferences ->
            val jsonString = preferences[HABITS_KEY]
            if (jsonString.isNullOrBlank()) return@edit

            val currentHabits: MutableList<Habit> = gson.fromJson(jsonString, habitListType)
            val wasRemoved = currentHabits.removeAll { it.id == habitId }

            if (wasRemoved) {
                preferences[HABITS_KEY] = gson.toJson(currentHabits)
                android.util.Log.d("HabitsRepository", "🗑️ Usunięto nawyk")
            }
        }
    }

    /**
     * Pobierz nawyk po ID
     */
    suspend fun getHabitById(habitId: String): Habit? {
        return try {
            val preferences = context.habitsDataStore.data.first()
            val jsonString = preferences[HABITS_KEY]

            if (!jsonString.isNullOrBlank()) {
                val habits: List<Habit> = gson.fromJson(jsonString, habitListType)
                habits.find { it.id == habitId }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitsRepository", "❌ Błąd pobierania nawyku: ${e.message}")
            null
        }
    }
}