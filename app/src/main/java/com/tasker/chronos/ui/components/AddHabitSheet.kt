// Plik: ui/components/AddHabitSheet.kt
package com.tasker.chronos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Habit
import com.tasker.chronos.data.models.HabitFrequency
import com.tasker.chronos.ui.theme.*
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (Habit) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var name by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.CODZIENNY) }
    var selectedWeekDays by remember { mutableStateOf(emptySet<String>()) }
    var selectedMonthDays by remember { mutableStateOf(emptySet<Int>()) }
    var hasReminder by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") }
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // === GRADIENT HEADER ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                HabitGreen.copy(alpha = 0.8f),
                                Color(0xFF8BC34A).copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Text(
                        "Nowy nawyk",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = textWithBlueGlow()
                    )
                }
            }

            // === SCROLLABLE CONTENT ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Nazwa
                AnimatedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nazwa nawyku",
                    icon = Icons.Default.FitnessCenter
                )

                // Częstotliwość
                Text(
                    text = "Częstotliwość",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

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

                // Wybór dni tygodnia
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

                // Wybór dni miesiąca
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

                // Przypomnienie
                GlassSurface {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    "Przypomnienie",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
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

                Spacer(modifier = Modifier.height(80.dp))
            }

            // === BOTTOM BUTTONS (STICKY) ===
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Anuluj", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val newHabit = Habit(
                                    name = name,
                                    weeklyDays = if (selectedFrequency == HabitFrequency.TYGODNIOWY)
                                        selectedWeekDays.toList() else emptyList(),
                                    monthlyDates = if (selectedFrequency == HabitFrequency.MIESIECZNY)
                                        selectedMonthDays.map { it.toString() } else emptyList(),
                                    hasReminder = hasReminder,
                                    reminderTime = if (hasReminder) reminderTime else null
                                )
                                onConfirm(newHabit)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Dodaj", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Time Picker
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
                        reminderTime = String.format(
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )
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
}