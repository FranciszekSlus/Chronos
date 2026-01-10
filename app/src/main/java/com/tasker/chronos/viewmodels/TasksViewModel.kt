    // Plik: viewmodels/TasksViewModel.kt
    package com.tasker.chronos.viewmodels

    import android.app.Application
    import androidx.lifecycle.AndroidViewModel
    import androidx.lifecycle.viewModelScope
    import com.tasker.chronos.data.models.Task
    import com.tasker.chronos.data.repository.TasksRepository
    import com.tasker.chronos.notifications.TaskReminderScheduler
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.SharingStarted
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.first
    import kotlinx.coroutines.flow.stateIn
    import kotlinx.coroutines.launch
    import java.time.LocalDateTime

    class TasksViewModel(application: Application) : AndroidViewModel(application) {
        private val repository = TasksRepository(application)

        // Wszystkie aktywne zadania
        val allTasks: StateFlow<List<Task>> = repository.getAllTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Zadania z Inbox (bez daty)
        val inboxTasks: StateFlow<List<Task>> = repository.getInboxTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Zaplanowane zadania (z datą)
        val scheduledTasks: StateFlow<List<Task>> = repository.getScheduledTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Archiwalne zadania
        val archivedTasks: StateFlow<List<Task>> = repository.getArchivedTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        fun addTask(task: Task) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addTask(task)

                if (task.hasReminder && task.date != null) {
                    TaskReminderScheduler.schedule(getApplication(), task)
                }
            }
        }

        fun updateTask(task: Task) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateTask(task)

                if (task.hasReminder && task.date != null && !task.isCompleted) {
                    TaskReminderScheduler.reschedule(getApplication(), task)
                } else {
                    TaskReminderScheduler.cancel(getApplication(), task)
                }

                android.util.Log.d("TasksViewModel", "✅ Zaktualizowano zadanie: ${task.title}")
            }
        }

        /**
         * ✅ NAPRAWIONE: Używa .first() zamiast .value
         */
        fun toggleTaskCompleted(taskId: String) {
            viewModelScope.launch(Dispatchers.IO) {
                android.util.Log.d("TasksViewModel", "🔍 Toggle dla ID: $taskId")

                // ✅ POPRAWKA: Pobierz świeże dane z Repository
                val allTasksList = repository.getAllTasks().first()
                val archivedTasksList = repository.getArchivedTasks().first()

                android.util.Log.d("TasksViewModel", "🔍 Aktywnych: ${allTasksList.size}, Archiwalnych: ${archivedTasksList.size}")

                val task = allTasksList.find { it.id == taskId }
                    ?: archivedTasksList.find { it.id == taskId }

                if (task == null) {
                    android.util.Log.e("TasksViewModel", "❌ Nie znaleziono zadania: $taskId")
                    android.util.Log.e("TasksViewModel", "❌ Dostępne ID w aktywnych: ${allTasksList.map { it.id }}")
                    android.util.Log.e("TasksViewModel", "❌ Dostępne ID w archiwum: ${archivedTasksList.map { it.id }}")
                    return@launch
                }

                android.util.Log.d("TasksViewModel", "✅ Znaleziono: ${task.title}, obecny stan: ${task.isCompleted}")

                val newCompletedStatus = !task.isCompleted
                val updatedTask = task.copy(
                    isCompleted = newCompletedStatus,
                    completedAt = if (newCompletedStatus) LocalDateTime.now().toString() else null
                )

                repository.updateTask(updatedTask)

                if (updatedTask.isCompleted) {
                    TaskReminderScheduler.cancel(getApplication(), updatedTask)
                    android.util.Log.d("TasksViewModel", "✅ Ukończono: ${task.title}")
                } else {
                    if (updatedTask.hasReminder && updatedTask.date != null) {
                        TaskReminderScheduler.schedule(getApplication(), updatedTask)
                    }
                    android.util.Log.d("TasksViewModel", "↩️ Przywrócono: ${task.title}")
                }
            }
        }

        fun deleteTask(task: Task) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.deleteTask(task.id)
                TaskReminderScheduler.cancel(getApplication(), task)
                android.util.Log.d("TasksViewModel", "🗑️ Usunięto zadanie: ${task.title}")
            }
        }

        fun clearArchive() {
            viewModelScope.launch(Dispatchers.IO) {
                repository.clearArchive()
                android.util.Log.d("TasksViewModel", "🗑️ Wyczyszczono archiwum")
            }
        }

        fun assignDateToTask(taskId: String, date: String?, time: String? = null) {
            viewModelScope.launch(Dispatchers.IO) {
                // ✅ POPRAWKA: Użyj .first()
                val allTasksList = repository.getAllTasks().first()
                val task = allTasksList.find { it.id == taskId }

                if (task == null) {
                    android.util.Log.e("TasksViewModel", "❌ Nie znaleziono zadania: $taskId")
                    return@launch
                }

                val updatedTask = task.copy(
                    date = date,
                    time = time
                )

                repository.updateTask(updatedTask)

                if (updatedTask.hasReminder) {
                    TaskReminderScheduler.reschedule(getApplication(), updatedTask)
                }

                android.util.Log.d("TasksViewModel", "📅 Przypisano datę do zadania: ${task.title}")
            }
        }
    }