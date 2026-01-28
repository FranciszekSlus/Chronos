package com.tasker.chronos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tasker.chronos.data.models.CustomEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.customEventsDataStore: DataStore<Preferences> by preferencesDataStore(name = "custom_events")

class CustomEventsRepository(private val context: Context) {

    private val EVENTS_KEY = stringPreferencesKey("custom_events_list")
    private val gson = Gson()
    private val eventListType = object : TypeToken<List<CustomEvent>>() {}.type

    fun getAllCustomEvents(): Flow<List<CustomEvent>> {
        return context.customEventsDataStore.data.map { preferences ->
            val jsonString = preferences[EVENTS_KEY]
            if (jsonString.isNullOrBlank()) {
                android.util.Log.d("CustomEventsRepo", "📭 Brak zapisanych wydarzeń")
                emptyList()
            } else {
                val events = gson.fromJson<List<CustomEvent>>(jsonString, eventListType)
                android.util.Log.d("CustomEventsRepo", "📦 Wczytano ${events.size} wydarzeń")
                events
            }
        }
    }

    suspend fun addEvent(event: CustomEvent) {
        context.customEventsDataStore.edit { preferences ->
            val jsonString = preferences[EVENTS_KEY]
            val currentEvents = if (jsonString.isNullOrBlank()) {
                mutableListOf()
            } else {
                gson.fromJson<MutableList<CustomEvent>>(jsonString, eventListType)
            }

            android.util.Log.d("CustomEventsRepo", "➕ Dodaję wydarzenie: ${event.title}")
            android.util.Log.d("CustomEventsRepo", "   ID: ${event.id}")
            android.util.Log.d("CustomEventsRepo", "   sourceGoalId: ${event.sourceGoalId}")  // ✅ DODAJ LOG
            android.util.Log.d("CustomEventsRepo", "   sourceMiniGoalId: ${event.sourceMiniGoalId}")  // ✅ DODAJ LOG

            currentEvents.add(event)
            preferences[EVENTS_KEY] = gson.toJson(currentEvents)

            android.util.Log.d("CustomEventsRepo", "✅ Zapisano, łącznie: ${currentEvents.size} wydarzeń")
        }
    }

    suspend fun updateEvent(event: CustomEvent) {
        context.customEventsDataStore.edit { preferences ->
            val jsonString = preferences[EVENTS_KEY]
            if (jsonString.isNullOrBlank()) return@edit

            val currentEvents: MutableList<CustomEvent> = gson.fromJson(jsonString, eventListType)
            val index = currentEvents.indexOfFirst { it.id == event.id }

            if (index != -1) {
                currentEvents[index] = event
                preferences[EVENTS_KEY] = gson.toJson(currentEvents)
                android.util.Log.d("CustomEventsRepo", "✅ Zaktualizowano: ${event.title}")
            }
        }
    }

    suspend fun deleteEvent(eventId: String) {
        android.util.Log.d("CustomEventsRepo", "🗑️ Usuwam wydarzenie: $eventId")

        context.customEventsDataStore.edit { preferences ->
            val jsonString = preferences[EVENTS_KEY]
            if (jsonString.isNullOrBlank()) {
                android.util.Log.w("CustomEventsRepo", "⚠️ Brak wydarzeń do usunięcia")
                return@edit
            }

            val currentEvents: MutableList<CustomEvent> = gson.fromJson(jsonString, eventListType)
            val sizeBefore = currentEvents.size

            currentEvents.removeAll { it.id == eventId }

            preferences[EVENTS_KEY] = gson.toJson(currentEvents)

            android.util.Log.d("CustomEventsRepo", "✅ Usunięto (było: $sizeBefore, jest: ${currentEvents.size})")
        }
    }

    fun getEventsForDate(date: String): Flow<List<CustomEvent>> {
        return getAllCustomEvents().map { events ->
            events.filter { it.date == date }.sortedBy { it.startTime }
        }
    }
}
