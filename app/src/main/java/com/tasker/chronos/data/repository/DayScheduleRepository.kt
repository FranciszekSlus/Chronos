package com.tasker.chronos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tasker.chronos.data.models.DaySchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dayScheduleDataStore: DataStore<Preferences> by preferencesDataStore(name = "day_schedules")

class DayScheduleRepository(private val context: Context) {

    private val SCHEDULES_KEY = stringPreferencesKey("schedules_list")
    private val gson = Gson()
    private val listType = object : TypeToken<List<DaySchedule>>() {}.type

    fun getAllSchedules(): Flow<List<DaySchedule>> {
        return context.dayScheduleDataStore.data.map { preferences ->
            val json = preferences[SCHEDULES_KEY]
            if (json.isNullOrBlank()) emptyList()
            else gson.fromJson(json, listType)
        }
    }

    suspend fun addSchedule(schedule: DaySchedule) {
        context.dayScheduleDataStore.edit { preferences ->
            val json = preferences[SCHEDULES_KEY]
            val list = if (json.isNullOrBlank()) mutableListOf()
            else gson.fromJson<MutableList<DaySchedule>>(json, listType)
            list.add(schedule)
            preferences[SCHEDULES_KEY] = gson.toJson(list)
        }
    }

    suspend fun updateSchedule(schedule: DaySchedule) {
        context.dayScheduleDataStore.edit { preferences ->
            val json = preferences[SCHEDULES_KEY] ?: return@edit
            val list: MutableList<DaySchedule> = gson.fromJson(json, listType)
            val index = list.indexOfFirst { it.id == schedule.id }
            if (index != -1) {
                list[index] = schedule
                preferences[SCHEDULES_KEY] = gson.toJson(list)
            }
        }
    }

    suspend fun deleteSchedule(scheduleId: String) {
        context.dayScheduleDataStore.edit { preferences ->
            val json = preferences[SCHEDULES_KEY] ?: return@edit
            val list: MutableList<DaySchedule> = gson.fromJson(json, listType)
            list.removeAll { it.id == scheduleId }
            preferences[SCHEDULES_KEY] = gson.toJson(list)
        }
    }
}