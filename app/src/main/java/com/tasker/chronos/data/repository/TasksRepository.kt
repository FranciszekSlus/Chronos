package com.tasker.chronos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.tasker.chronos.data.models.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.tasksDataStore: DataStore<Preferences> by preferencesDataStore(name = "tasks")

class TasksRepository(context: Context) {

    private val appContext = context.applicationContext
    private val TASKS_KEY = stringPreferencesKey("tasks_list")
    private val ARCHIVED_TASKS_KEY = stringPreferencesKey("archived_tasks_list")
    private val gson: Gson = GsonBuilder().create()
    private val taskListType = object : TypeToken<List<Task>>() {}.type
    private val mutex = Mutex()

    fun getAllTasks(): Flow<List<Task>> {
        return appContext.tasksDataStore.data.map { preferences ->
            parseTaskList(preferences[TASKS_KEY]).filter { !it.isCompleted }
        }
    }

    suspend fun getAllTasksAsList(): List<Task> = mutex.withLock {
        val preferences = appContext.tasksDataStore.data.first()
        parseTaskList(preferences[TASKS_KEY])
    }

    suspend fun getArchivedTasksAsList(): List<Task> = mutex.withLock {
        val preferences = appContext.tasksDataStore.data.first()
        parseTaskList(preferences[ARCHIVED_TASKS_KEY])
    }

    fun getInboxTasks(): Flow<List<Task>> {
        return getAllTasks().map { tasks -> tasks.filter { it.date == null } }
    }

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

    fun getArchivedTasks(): Flow<List<Task>> {
        return appContext.tasksDataStore.data.map { preferences ->
            parseTaskList(preferences[ARCHIVED_TASKS_KEY])
        }
    }

    private fun parseTaskList(json: String?): MutableList<Task> {
        if (json.isNullOrBlank()) return mutableListOf()
        return try {
            val parsed: List<Task>? = gson.fromJson(json, taskListType)
            parsed.orEmpty().mapNotNull { task ->
                try {
                    if (task.id.isBlank()) null else task
                } catch (_: Exception) {
                    null
                }
            }.toMutableList()
        } catch (e: Exception) {
            android.util.Log.e("TasksRepository", "❌ Błąd parsowania listy: ${e.message}", e)
            mutableListOf()
        }
    }

    suspend fun addTask(task: Task) = mutex.withLock {
        appContext.tasksDataStore.edit { preferences ->
            val currentTasks = parseTaskList(preferences[TASKS_KEY])
            currentTasks.removeAll { it.id == task.id }
            currentTasks.add(task)
            preferences[TASKS_KEY] = gson.toJson(currentTasks)
        }
        android.util.Log.d("TasksRepository", "➕ Dodano: ${task.title} (${task.id})")
    }

    suspend fun updateTask(task: Task) = mutex.withLock {
        appContext.tasksDataStore.edit { preferences ->
            val currentTasks = parseTaskList(preferences[TASKS_KEY])
            val archivedTasks = parseTaskList(preferences[ARCHIVED_TASKS_KEY])

            val inCurrent = currentTasks.indexOfFirst { it.id == task.id }
            val inArchived = archivedTasks.indexOfFirst { it.id == task.id }

            when {
                task.isCompleted -> {
                    if (inCurrent >= 0) currentTasks.removeAt(inCurrent)
                    archivedTasks.removeAll { it.id == task.id }
                    archivedTasks.add(0, task)
                }
                else -> {
                    if (inArchived >= 0) archivedTasks.removeAt(inArchived)
                    if (inCurrent >= 0) {
                        currentTasks[inCurrent] = task
                    } else {
                        currentTasks.add(task)
                    }
                }
            }

            preferences[TASKS_KEY] = gson.toJson(currentTasks)
            preferences[ARCHIVED_TASKS_KEY] = gson.toJson(archivedTasks)
        }

        val verify = appContext.tasksDataStore.data.first().let { prefs ->
            parseTaskList(prefs[TASKS_KEY]).find { it.id == task.id }
                ?: parseTaskList(prefs[ARCHIVED_TASKS_KEY]).find { it.id == task.id }
        }
        android.util.Log.d(
            "TasksRepository",
            "✏️ Update verify id=${task.id} savedTitle=${verify?.title} expected=${task.title} ok=${verify?.title == task.title}"
        )
        if (verify?.title != task.title) {
            throw IllegalStateException("Task update did not persist for id=${task.id}")
        }
    }

    suspend fun replaceActiveAndArchived(active: List<Task>, archived: List<Task>) = mutex.withLock {
        appContext.tasksDataStore.edit { preferences ->
            preferences[TASKS_KEY] = gson.toJson(active)
            preferences[ARCHIVED_TASKS_KEY] = gson.toJson(archived)
        }
    }

    suspend fun deleteTask(taskId: String) = mutex.withLock {
        appContext.tasksDataStore.edit { preferences ->
            val currentTasks = parseTaskList(preferences[TASKS_KEY])
            val archivedTasks = parseTaskList(preferences[ARCHIVED_TASKS_KEY])
            currentTasks.removeAll { it.id == taskId }
            archivedTasks.removeAll { it.id == taskId }
            preferences[TASKS_KEY] = gson.toJson(currentTasks)
            preferences[ARCHIVED_TASKS_KEY] = gson.toJson(archivedTasks)
        }
    }

    suspend fun clearArchive() = mutex.withLock {
        appContext.tasksDataStore.edit { preferences ->
            preferences.remove(ARCHIVED_TASKS_KEY)
        }
    }
}
