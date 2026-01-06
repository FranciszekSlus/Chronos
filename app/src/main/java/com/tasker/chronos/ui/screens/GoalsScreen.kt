// Plik: ui/screens/GoalsScreen.kt
package com.tasker.chronos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Goal
import com.tasker.chronos.ui.components.GoalItem
import com.tasker.chronos.ui.dialogs.AddGoalDialog
import com.tasker.chronos.viewmodels.GoalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    goalsViewModel: GoalsViewModel,
    onNavigateToGoalDetails: (Goal) -> Unit
) {
    val activeGoals by goalsViewModel.activeGoals.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cele", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj cel")
            }
        }
    ) { padding ->
        if (activeGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎖️", fontSize = 64.sp)
                    Text(
                        "Brak celów",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Dodaj swój pierwszy cel!")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeGoals, key = { it.id }) { goal ->
                    GoalItem(
                        goal = goal,
                        onGoalClick = {
                            onNavigateToGoalDetails(goal)
                        },
                        onGoalLongClick = {
                            onNavigateToGoalDetails(goal)
                        }
                    )
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { goal -> // Przyjmuje Goal obiekt!
                goalsViewModel.addGoal(goal)
                showAddDialog = false
            }
        )
    }
}