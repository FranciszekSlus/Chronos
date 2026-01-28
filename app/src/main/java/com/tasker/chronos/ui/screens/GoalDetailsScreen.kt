// Plik: ui/screens/GoalDetailsScreen.kt
package com.tasker.chronos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Goal
import com.tasker.chronos.data.models.MiniGoal
import com.tasker.chronos.viewmodels.EventsViewModel
import com.tasker.chronos.viewmodels.GoalsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailsScreen(
    goal: Goal,
    goalsViewModel: GoalsViewModel,
    onNavigateBack: () -> Unit,
    onOpenEditSheet: () -> Unit = {},
    eventsViewModel: EventsViewModel
) {
    var showAddMiniGoalDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(goal.notes) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły celu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Usuń cel", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nagłówek celu
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = goal.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (goal.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = goal.description,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (goal.endDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )

                            val dateText = try {
                                val date = LocalDate.parse(goal.endDate)
                                "Do: " + date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                            } catch (e: Exception) {
                                goal.endDate
                            }

                            Text(
                                text = dateText,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Pasek postępu
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Postęp", fontWeight = FontWeight.Bold)
                        Text(
                            "${goal.getProgressPercentage()}%",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { goal.getProgress() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = Color(0xFFFF9800),
                    )
                }
            }

            // Mini-cele
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mini-cele (${goal.miniGoals.count { it.isCompleted }}/${goal.miniGoals.size})",
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { showAddMiniGoalDialog = true }) {
                            Icon(Icons.Default.Add, "Dodaj mini-cel")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (goal.miniGoals.isEmpty()) {
                        Text(
                            "Brak mini-celów. Dodaj pierwszy!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    } else {
                        goal.miniGoals.forEach { miniGoal ->
                            MiniGoalItem(
                                miniGoal = miniGoal,
                                onToggle = {
                                    goalsViewModel.toggleMiniGoalCompleted(goal.id, miniGoal.id)
                                },
                                onDelete = {
                                    goalsViewModel.deleteMiniGoal(goal.id, miniGoal.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Notatki
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Notatki", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Dodaj notatki...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 10
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            goalsViewModel.updateNotes(goal.id, notes)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Zapisz notatki")
                    }
                }
            }
        }
    }

    // Dialog dodawania mini-celu
    if (showAddMiniGoalDialog) {
        var miniGoalTitle by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMiniGoalDialog = false },
            title = { Text("Nowy mini-cel") },
            text = {
                OutlinedTextField(
                    value = miniGoalTitle,
                    onValueChange = { miniGoalTitle = it },
                    label = { Text("Tytuł mini-celu") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (miniGoalTitle.isNotBlank()) {
                            goalsViewModel.addMiniGoal(
                                goal.id,
                                MiniGoal(title = miniGoalTitle)
                            )
                            showAddMiniGoalDialog = false
                        }
                    },
                    enabled = miniGoalTitle.isNotBlank()
                ) {
                    Text("Dodaj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMiniGoalDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Dialog usuwania celu
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń cel") },
            text = { Text("Czy na pewno chcesz usunąć ten cel? Tej operacji nie można cofnąć.") },
            confirmButton = {
                Button(
                    onClick = {
                        goalsViewModel.deleteGoal(goal, eventsViewModel)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun MiniGoalItem(
    miniGoal: MiniGoal,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = miniGoal.isCompleted,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = miniGoal.title,
                style = if (miniGoal.isCompleted) {
                    MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (miniGoal.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Usuń",
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}