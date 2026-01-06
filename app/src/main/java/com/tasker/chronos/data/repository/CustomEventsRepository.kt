// Plik: data/repository/CustomEventsRepository.kt - WERSJA DIAGNOSTYCZNA
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

    /**
     * Pobiera wszystkie własne wydarzenia
     */
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

    /**
     * Dodaje nowe wydarzenie
     */
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
            android.util.Log.d("CustomEventsRepo", "   Czas: ${event.startTime} - ${event.endTime}")

            currentEvents.add(event)
            preferences[EVENTS_KEY] = gson.toJson(currentEvents)

            android.util.Log.d("CustomEventsRepo", "✅ Zapisano, łącznie: ${currentEvents.size} wydarzeń")
        }
    }

    /**
     * Aktualizuje wydarzenie
     */
    suspend fun updateEvent(event: CustomEvent) {
        context.customEventsDataStore.edit { preferences ->
            val jsonString = preferences[EVENTS_KEY]
            if (jsonString.isNullOrBlank()) {
                android.util.Log.e("CustomEventsRepo", "❌ Brak wydarzeń do aktualizacji!")
                return@edit
            }

            val currentEvents: MutableList<CustomEvent> = gson.fromJson(jsonString, eventListType)
            val index = currentEvents.indexOfFirst { it.id == event.id }

            android.util.Log.d("CustomEventsRepo", "════════════════════════════════")
            android.util.Log.d("CustomEventsRepo", "🔄 Aktualizacja wydarzenia")
            android.util.Log.d("CustomEventsRepo", "   ID: ${event.id}")
            android.util.Log.d("CustomEventsRepo", "   Znaleziono na indeksie: $index")

            if (index != -1) {
                val oldEvent = currentEvents[index]
                android.util.Log.d("CustomEventsRepo", "   PRZED: ${oldEvent.startTime} - ${oldEvent.endTime}")
                android.util.Log.d("CustomEventsRepo", "   PO: ${event.startTime} - ${event.endTime}")

                currentEvents[index] = event
                preferences[EVENTS_KEY] = gson.toJson(currentEvents)

                android.util.Log.d("CustomEventsRepo", "✅ Zaktualizowano pomyślnie")
            } else {
                android.util.Log.e("CustomEventsRepo", "❌ NIE ZNALEZIONO wydarzenia o ID: ${event.id}")
                android.util.Log.e("CustomEventsRepo", "   Dostępne ID:")
                currentEvents.forEach {
                    android.util.Log.e("CustomEventsRepo", "     - ${it.id} (${it.title})")
                }
            }
            android.util.Log.d("CustomEventsRepo", "════════════════════════════════")
        }
    }

    /**
     * Usuwa wydarzenie
     */
    suspend fun deleteEvent(eventId: String) {
        context.customEventsDataStore.edit { preferences ->
            val jsonString = preferences[EVENTS_KEY]
            if (jsonString.isNullOrBlank()) return@edit

            val currentEvents: MutableList<CustomEvent> = gson.fromJson(jsonString, eventListType)
            val wasRemoved = currentEvents.removeAll { it.id == eventId }
            if (wasRemoved) {
                preferences[EVENTS_KEY] = gson.toJson(currentEvents)
                android.util.Log.d("CustomEventsRepo", "🗑️ Usunięto wydarzenie: $eventId")
            }
        }
    }

    /**
     * Pobiera wydarzenia dla konkretnej daty
     */
    fun getEventsForDate(date: String): Flow<List<CustomEvent>> {
        return getAllCustomEvents().map { events ->
            events.filter { it.date == date }
                .sortedBy { it.startTime }
        }
    }
}