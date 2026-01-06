//    package com.tasker.chronos.viewmodels
//
//    import android.app.Application
//    import androidx.lifecycle.AndroidViewModel
//    import androidx.lifecycle.viewModelScope
//    import com.tasker.chronos.data.models.Task
//    import com.tasker.chronos.data.models.TaskImportance
//    import com.tasker.chronos.data.repository.TasksRepository
//    import kotlinx.coroutines.Dispatchers
//    import kotlinx.coroutines.flow.MutableStateFlow
//    import kotlinx.coroutines.flow.StateFlow
//    import kotlinx.coroutines.flow.asStateFlow
//    import kotlinx.coroutines.launch
//    import java.time.LocalDate
//    import java.time.LocalTime
//    import java.util.UUID
//
//    // Zmieniliśmy nazwę klasy
//    class TasksViewModel(application: Application) : AndroidViewModel(application) {
//        private val repository = TasksRepository(application)
//
//        private val _tasks = MutableStateFlow<List<Task>>(emptyList())
//        val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
//
//        init {
//            loadTasks()
//        }
//
//        private fun loadTasks() {
//            viewModelScope.launch {
//                repository.getTasks().collect { tasksList ->
//                    _tasks.value = tasksList.sortedWith(
//                        compareByDescending<Task> { it.priority.value }.thenBy { it.date }.thenBy { it.time }
//                    )
//                }
//            }
//        }
//
//        private fun saveCurrentTasks() {
//            viewModelScope.launch(Dispatchers.IO) {
//                repository.saveTasks(_tasks.value)
//            }
//        }
//
//        fun addTask(
//            name: String,
//            description: String,
//            date: LocalDate?,
//            time: LocalTime?,
//            reminder: Boolean,
//            priority: TaskImportance
//        ) {
//            val newTask = Task(
//                id = UUID.randomUUID().toString(),
//                title = name,
//                description = description,
//                date = date,
//                time = time,
//                hasReminder = reminder,
//                priority = priority
//            )
//            _tasks.value = (_tasks.value + newTask).sortedWith(
//                compareByDescending<Task> { it.priority.value }.thenBy { it.date }.thenBy { it.time }
//            )
//            saveCurrentTasks()
//        }
//
//        fun updateTask(task: Task, isCompleted: Boolean) {
//            _tasks.value = _tasks.value.map {
//                if (it.id == task.id) {
//                    it.copy(isCompleted = isCompleted)
//                } else {
//                    it
//                }
//            }
//            // Nie sortujemy ponownie przy aktualizacji, aby element nie "skakał" po liście
//            saveCurrentTasks()
//        }
//
//        fun deleteTask(task: Task) {
//            _tasks.value = _tasks.value.filter { it.id != task.id }
//            saveCurrentTasks()
//        }
//    }