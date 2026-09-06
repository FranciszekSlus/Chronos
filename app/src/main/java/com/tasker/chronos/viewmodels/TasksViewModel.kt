package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.models.Task
import com.tasker.chronos.data.repository.TasksRepository
import com.tasker.chronos.notifications.TaskReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TasksRepository(application)

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _archivedTasks = MutableStateFlow<List<Task>>(emptyList())

    val allTasks: StateFlow<List<Task>> = _allTasks
    val archivedTasks: StateFlow<List<Task>> = _archivedTasks

    val inboxTasks: StateFlow<List<Task>> = _allTasks
        .map { tasks -> tasks.filter { it.date == null } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val scheduledTasks: StateFlow<List<Task>> = _allTasks
        .map { tasks ->
            tasks.filter { it.date != null }
                .sortedWith(
                    compareBy<Task> { it.date }
                        .thenByDescending { it.priority }
                        .thenBy { it.time ?: "23:59" }
                )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val storedActive = repository.getAllTasksAsList()
            val active = storedActive.filter { !it.isCompleted }
            val stragglers = storedActive.filter { it.isCompleted }
            val archived = repository.getArchivedTasksAsList() + stragglers
            _allTasks.value = active
            _archivedTasks.value = archived.distinctBy { it.id }
            if (stragglers.isNotEmpty()) {
                repository.replaceActiveAndArchived(_allTasks.value, _archivedTasks.value)
            }
            android.util.Log.d(
                "TasksViewModel",
                "Loaded active=${active.size} archived=${_archivedTasks.value.size}"
            )
        }
    }

    fun findTask(id: String): Task? =
        _allTasks.value.find { it.id == id } ?: _archivedTasks.value.find { it.id == id }

    /** Updates in-memory list immediately (UI), then persists on IO. */
    fun updateTask(task: Task) {
        applyUpdateInMemory(task)
        viewModelScope.launch(Dispatchers.IO) {
            repository.replaceActiveAndArchived(_allTasks.value, _archivedTasks.value)
            if (task.isCompleted || !task.hasReminder || task.date == null) {
                TaskReminderScheduler.cancel(getApplication(), task)
            } else {
                TaskReminderScheduler.reschedule(getApplication(), task)
            }
            val verify = findTask(task.id)
            android.util.Log.d(
                "TasksViewModel",
                "Saved '${task.title}' verify='${verify?.title}' ok=${verify?.title == task.title}"
            )
        }
    }

    private fun applyUpdateInMemory(task: Task) {
        if (task.isCompleted) {
            _allTasks.value = _allTasks.value.filter { it.id != task.id }
            _archivedTasks.value =
                listOf(task) + _archivedTasks.value.filter { it.id != task.id }
        } else {
            _archivedTasks.value = _archivedTasks.value.filter { it.id != task.id }
            val exists = _allTasks.value.any { it.id == task.id }
            _allTasks.value = if (exists) {
                _allTasks.value.map { if (it.id == task.id) task else it }
            } else {
                _allTasks.value + task
            }
        }
    }

    fun addTask(task: Task) {
        _allTasks.value = _allTasks.value.filter { it.id != task.id } + task
        viewModelScope.launch(Dispatchers.IO) {
            repository.replaceActiveAndArchived(_allTasks.value, _archivedTasks.value)
            if (task.hasReminder && task.date != null) {
                TaskReminderScheduler.schedule(getApplication(), task)
            }
        }
    }

    fun toggleTaskCompleted(taskId: String) {
        val task = findTask(taskId) ?: return
        updateTask(
            task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) LocalDateTime.now().toString() else null
            )
        )
    }

    fun deleteTask(task: Task) {
        _allTasks.value = _allTasks.value.filter { it.id != task.id }
        _archivedTasks.value = _archivedTasks.value.filter { it.id != task.id }
        viewModelScope.launch(Dispatchers.IO) {
            repository.replaceActiveAndArchived(_allTasks.value, _archivedTasks.value)
            TaskReminderScheduler.cancel(getApplication(), task)
        }
    }

    fun clearArchive() {
        _archivedTasks.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            repository.replaceActiveAndArchived(_allTasks.value, _archivedTasks.value)
        }
    }

    fun assignDateToTask(taskId: String, date: String?, time: String? = null) {
        val task = _allTasks.value.find { it.id == taskId } ?: return
        updateTask(task.copy(date = date, time = time))
    }
}
