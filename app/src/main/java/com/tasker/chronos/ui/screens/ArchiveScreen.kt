package com.tasker.chronos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasker.chronos.data.models.Goal
import com.tasker.chronos.data.models.Task
import com.tasker.chronos.ui.components.GoalItem
import com.tasker.chronos.ui.components.TaskItem
import com.tasker.chronos.viewmodels.EventsViewModel
import com.tasker.chronos.viewmodels.GoalsViewModel
import com.tasker.chronos.viewmodels.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    tasksViewModel: TasksViewModel = viewModel(),
    goalsViewModel: GoalsViewModel = viewModel(),
    eventsViewModel: EventsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val archivedTasks by tasksViewModel.archivedTasks.collectAsState()
    val archivedGoals by goalsViewModel.archivedGoals.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Zadania", "Cele")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archiwum") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Wyczyść archiwum")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                "$title (${if (index == 0) archivedTasks.size else archivedGoals.size})"
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> ArchivedTasksTab(
                    tasks = archivedTasks,
                    onRestoreTask = { task ->
                        tasksViewModel.updateTask(task.copy(isCompleted = false))
                    },
                    onDeleteTask = { task ->
                        tasksViewModel.deleteTask(task)
                    }
                )
                1 -> ArchivedGoalsTab(
                    goals = archivedGoals,
                    onRestoreGoal = { goal ->
                        // Cofnij jeden mini-cel żeby nie było 100%
                        val updatedMiniGoals = goal.miniGoals.mapIndexed { index, miniGoal ->
                            if (index == goal.miniGoals.lastIndex && miniGoal.isCompleted) {
                                miniGoal.copy(isCompleted = false)
                            } else {
                                miniGoal
                            }
                        }
                        goalsViewModel.updateGoal(goal.copy(miniGoals = updatedMiniGoals))
                    },
                    onDeleteGoal = { goal ->
                        goalsViewModel.deleteGoal(goal, eventsViewModel)// ✅ Przekaż cały Goal
                    }
                )
            }
        }
    }

    // Dialog czyszczenia archiwum
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Wyczyść archiwum") },
            text = {
                Text("Czy na pewno chcesz usunąć wszystkie ${if (selectedTab == 0) "zadania" else "cele"} z archiwum? Tej operacji nie można cofnąć.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (selectedTab) {
                            0 -> tasksViewModel.clearArchive()
                            1 -> goalsViewModel.clearArchive()
                        }
                        showClearDialog = false
                    }
                ) {
                    Text("Usuń", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun ArchivedTasksTab(
    tasks: List<Task>,
    onRestoreTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Archive,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    "Brak ukończonych zadań",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                ArchivedTaskItem(
                    task = task,
                    onRestore = { onRestoreTask(task) },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }
    }
}

@Composable
fun ArchivedGoalsTab(
    goals: List<Goal>,
    onRestoreGoal: (Goal) -> Unit,
    onDeleteGoal: (Goal) -> Unit
) {
    if (goals.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    "Brak ukończonych celów",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(goals, key = { it.id }) { goal ->
                ArchivedGoalItem(
                    goal = goal,
                    onRestore = { onRestoreGoal(goal) },
                    onDelete = { onDeleteGoal(goal) }
                )
            }
        }
    }
}

@Composable
fun ArchivedTaskItem(
    task: Task,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (task.completedAt != null) {
                    Text(
                        text = "Ukończono: ${formatDateTime(task.completedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = "Przywróć",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ArchivedGoalItem(
    goal: Goal,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "✅ Ukończono w 100%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(onClick = onRestore) {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = "Przywróć",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${goal.miniGoals.size} mini-celów",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatDateTime(dateTime: String): String {
    return try {
        val parsed = java.time.LocalDateTime.parse(dateTime)
        "${parsed.dayOfMonth}.${parsed.monthValue}.${parsed.year} ${parsed.hour}:${parsed.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        dateTime
    }
}