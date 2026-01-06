// Plik: data/repository/TasksRepository.kt
package com.tasker.chronos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tasker.chronos.data.models.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.tasksDataStore: DataStore<Preferences> by preferencesDataStore(name = "tasks")

class TasksRepository(private val context: Context) {

    private val TASKS_KEY = stringPreferencesKey("tasks_list")
    private val ARCHIVED_TASKS_KEY = stringPreferencesKey("archived_tasks_list")
    private val gson = Gson()
    private val taskListType = object : TypeToken<List<Task>>() {}.type
    private val mutex = Mutex()

    /**
     * Pobierz wszystkie aktywne zadania (nie ukończone)
     */
    fun getAllTasks(): Flow<List<Task>> {
        return context.tasksDataStore.data.map { preferences ->
            val jsonString = preferences[TASKS_KEY]
            if (jsonString.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    val tasks: List<Task> = gson.fromJson(jsonString, taskListType)
                    tasks.filter { !it.isCompleted }
                } catch (e: Exception) {
                    android.util.Log.e("TasksRepository", "❌ Błąd deserializacji: ${e.message}")
                    emptyList()
                }
            }
        }
    }

    /**
     * Pobierz zadania z Inbox (bez daty)
     */
    fun getInboxTasks(): Flow<List<Task>> {
        return getAllTasks().map { tasks ->
            tasks.filter { it.date == null }
        }
    }

    /**
     * Pobierz zadania z datą
     */
    fun getScheduledTasks(): Flow<List<Task>> {
        return getAllTasks().map { tasks ->
            tasks.filter { it.date != null }
                .sortedWith(
                    compareBy<Task> { it.date }
                        .thenByDescending { it.priority }
                        .thenBy { it.time ?: "23:59" }
                )
        }
    }

    /**
     * Pobierz archiwalne zadania (ukończone)
     */
    fun getArchivedTasks(): Flow<List<Task>> {
        return context.tasksDataStore.data.map { preferences ->
            val jsonString = preferences[ARCHIVED_TASKS_KEY]
            if (jsonString.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    gson.fromJson(jsonString, taskListType)
                } catch (e: Exception) {
                    android.util.Log.e("TasksRepository", "❌ Błąd deserializacji archiwum: ${e.message}")
                    emptyList()
                }
            }
        }
    }

    suspend fun addTask(task: Task) = mutex.withLock {
        context.tasksDataStore.edit { preferences ->
            val jsonString = preferences[TASKS_KEY]
            val currentTasks = if (jsonString.isNullOrBlank()) {
                mutableListOf()
            } else {
                gson.fromJson<MutableList<Task>>(jsonString, taskListType)
            }
            currentTasks.add(task)
            preferences[TASKS_KEY] = gson.toJson(currentTasks)
        }
    }

    suspend fun updateTask(task: Task) = mutex.withLock {
        try {
            context.tasksDataStore.edit { preferences ->
                val currentJsonString = preferences[TASKS_KEY] ?: ""
                val archivedJsonString = preferences[ARCHIVED_TASKS_KEY] ?: ""

                val currentTasks: MutableList<Task> = if (currentJsonString.isBlank()) {
                    mutableListOf()
                } else {
                    try {
                        gson.fromJson(currentJsonString, taskListType)
                    } catch (e: Exception) {
                        android.util.Log.e("TasksRepository", "❌ Błąd parsowania current: ${e.message}")
                        mutableListOf()
                    }
                }

                val archivedTasks: MutableList<Task> = if (archivedJsonString.isBlank()) {
                    mutableListOf()
                } else {
                    try {
                        gson.fromJson(archivedJsonString, taskListType)
                    } catch (e: Exception) {
                        android.util.Log.e("TasksRepository", "❌ Błąd parsowania archived: ${e.message}")
                        mutableListOf()
                    }
                }

                val indexInCurrent = currentTasks.indexOfFirst { it.id == task.id }
                val indexInArchived = archivedTasks.indexOfFirst { it.id == task.id }

                when {
                    indexInCurrent != -1 -> {
                        if (task.isCompleted) {
                            currentTasks.removeAt(indexInCurrent)
                            archivedTasks.add(0, task)
                            android.util.Log.d("TasksRepository", "📦 Przeniesiono do archiwum: ${task.title}")
                        } else {
                            currentTasks[indexInCurrent] = task
                            android.util.Log.d("TasksRepository", "✏️ Zaktualizowano: ${task.title}")
                        }
                    }

                    indexInArchived != -1 -> {
                        if (!task.isCompleted) {
                            archivedTasks.removeAt(indexInArchived)
                            currentTasks.add(task)
                            android.util.Log.d("TasksRepository", "↩️ Przywrócono: ${task.title}")
                        } else {
                            archivedTasks[indexInArchived] = task
                            android.util.Log.d("TasksRepository", "✏️ Zaktualizowano w archiwum: ${task.title}")
                        }
                    }

                    else -> {
                        android.util.Log.w("TasksRepository", "⚠️ Task nie znaleziony, dodaję jako nowy")
                        if (task.isCompleted) {
                            archivedTasks.add(0, task)
                        } else {
                            currentTasks.add(task)
                        }
                    }
                }

                preferences[TASKS_KEY] = gson.toJson(currentTasks)
                preferences[ARCHIVED_TASKS_KEY] = gson.toJson(archivedTasks)
            }
        } catch (e: Exception) {
            android.util.Log.e("TasksRepository", "❌ Błąd updateTask: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteTask(taskId: String) = mutex.withLock {
        context.tasksDataStore.edit { preferences ->
            val currentJsonString = preferences[TASKS_KEY]
            val archivedJsonString = preferences[ARCHIVED_TASKS_KEY]

            if (currentJsonString != null) {
                val currentTasks: MutableList<Task> = gson.fromJson(currentJsonString, taskListType)
                val wasRemoved = currentTasks.removeAll { it.id == taskId }
                if (wasRemoved) {
                    preferences[TASKS_KEY] = gson.toJson(currentTasks)
                    android.util.Log.d("TasksRepository", "🗑️ Usunięto z aktywnych")
                }
            }

            if (archivedJsonString != null) {
                val archivedTasks: MutableList<Task> = gson.fromJson(archivedJsonString, taskListType)
                val wasRemoved = archivedTasks.removeAll { it.id == taskId }
                if (wasRemoved) {
                    preferences[ARCHIVED_TASKS_KEY] = gson.toJson(archivedTasks)
                    android.util.Log.d("TasksRepository", "🗑️ Usunięto z archiwum")
                }
            }
        }
    }

    suspend fun clearArchive() = mutex.withLock {
        context.tasksDataStore.edit { preferences ->
            preferences.remove(ARCHIVED_TASKS_KEY)
            android.util.Log.d("TasksRepository", "🗑️ Wyczyszczono archiwum")
        }
    }
}