// Plik: ui/components/EditHabitSheet.kt
package com.tasker.chronos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Habit
import com.tasker.chronos.data.models.HabitFrequency
import kotlinx.coroutines.delay
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitSheet(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
    onDelete: (Habit) -> Unit
) {
    var name by remember { mutableStateOf(habit.name) }
    var selectedFrequency by remember {
        mutableStateOf(
            when {
                habit.weeklyDays.isNotEmpty() -> HabitFrequency.TYGODNIOWY
                habit.monthlyDates.isNotEmpty() -> HabitFrequency.MIESIECZNY
                else -> HabitFrequency.CODZIENNY
            }
        )
    }
    var selectedWeekDays by remember { mutableStateOf(habit.weeklyDays.toSet()) }
    var selectedMonthDays by remember { mutableStateOf(habit.monthlyDates.mapNotNull { it.toIntOrNull() }.toSet()) }
    var hasReminder by remember { mutableStateOf(habit.hasReminder) }
    var reminderTime by remember { mutableStateOf(habit.reminderTime ?: "09:00") }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(400, easing = EaseOutCubic)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header z gradientem i streak
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50).copy(alpha = 0.3f),
                                    Color(0xFF8BC34A).copy(alpha = 0.2f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Edytuj nawyk",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (habit.streak > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("🔥", fontSize = 28.sp)
                                        Text(
                                            "${habit.streak} dni",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF6B35)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.15f))
                                    .clickable { showDeleteDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Usuń",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (habit.points > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "+${habit.points} punktów",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFFF6B35)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nazwa nawyku",
                    icon = Icons.Default.FitnessCenter
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Częstotliwość",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HabitFrequency.values().forEach { type ->
                        AnimatedFrequencyButton(
                            type = type,
                            isSelected = selectedFrequency == type,
                            onClick = { selectedFrequency = type },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = selectedFrequency == HabitFrequency.TYGODNIOWY,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    WeekDaySelector(
                        selectedDays = selectedWeekDays,
                        onDaysChanged = { selectedWeekDays = it }
                    )
                }

                AnimatedVisibility(
                    visible = selectedFrequency == HabitFrequency.MIESIECZNY,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    MonthDaySelector(
                        selectedDays = selectedMonthDays,
                        onDaysChanged = { selectedMonthDays = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlassSurface {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text("Przypomnienie", fontWeight = FontWeight.Medium)
                            }

                            Switch(
                                checked = hasReminder,
                                onCheckedChange = { hasReminder = it }
                            )
                        }

                        AnimatedVisibility(
                            visible = hasReminder,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            GlassButton(
                                onClick = { showTimePicker = true },
                                icon = Icons.Default.Schedule,
                                text = "O $reminderTime",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Anuluj", fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                // POPRAWKA: Zachowaj wszystkie pola nawyku
                                onSave(
                                    habit.copy(
                                        name = name,
                                        weeklyDays = if (selectedFrequency == HabitFrequency.TYGODNIOWY)
                                            selectedWeekDays.toList() else emptyList(),
                                        monthlyDates = if (selectedFrequency == HabitFrequency.MIESIECZNY)
                                            selectedMonthDays.map { it.toString() } else emptyList(),
                                        hasReminder = hasReminder,
                                        reminderTime = if (hasReminder) reminderTime else null
                                        // Nie nadpisuj: completionDates, streak, lastCompletionDate, points
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Zapisz", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = try { LocalTime.parse(reminderTime).hour } catch (e: Exception) { 9 },
            initialMinute = try { LocalTime.parse(reminderTime).minute } catch (e: Exception) { 0 }
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń nawyk") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Czy na pewno chcesz usunąć ten nawyk?")
                    if (habit.streak > 0) {
                        Text(
                            "Stracisz streak ${habit.streak} dni! 🔥",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(habit)
                        showDeleteDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") }
            }
        )
    }
}