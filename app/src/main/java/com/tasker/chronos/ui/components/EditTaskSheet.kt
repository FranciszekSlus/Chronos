// Plik: ui/components/EditTaskSheet.kt
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Task
import com.tasker.chronos.data.models.TaskPriority
import com.tasker.chronos.data.models.ReminderType
import com.tasker.chronos.data.models.toColor
import com.tasker.chronos.data.models.toDisplayName
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskSheet(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var selectedDate by remember { mutableStateOf(task.date) }
    var selectedTime by remember { mutableStateOf(task.time) }
    var selectedPriority by remember { mutableStateOf(task.priority) }
    var hasReminder by remember { mutableStateOf(task.hasReminder) }

    var reminderType by remember { mutableStateOf(task.reminderType) }
    var reminderMinutesBefore by remember { mutableStateOf(task.reminderMinutesBefore) }
    var reminderCustomTime by remember { mutableStateOf(task.reminderCustomTime ?: "09:00") }
    var reminderCustomDays by remember { mutableStateOf(task.reminderCustomDays ?: 1) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }
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
            ),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    selectedPriority.toColor().copy(alpha = 0.3f),
                                    selectedPriority.toColor().copy(alpha = 0.1f)
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
                            Text(
                                "Edytuj zadanie",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

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

                        if (task.isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Ukończone",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tytuł
                AnimatedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Tytuł",
                    icon = Icons.Default.Title
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Opis
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Termin",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        onClick = { showDatePicker = true },
                        icon = Icons.Default.CalendarToday,
                        text = selectedDate?.let {
                            try {
                                LocalDate.parse(it).toString()
                            } catch (e: Exception) {
                                "Data"
                            }
                        } ?: "Wybierz datę",
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedDate != null) {
                        GlassButton(
                            onClick = { showTimePicker = true },
                            icon = Icons.Default.Schedule,
                            text = selectedTime ?: "Godzina",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Priorytet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ Proste przyciski priorytet (bez AnimatedPriorityButton)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TaskPriority.values().forEach { priority ->
                        Button(
                            onClick = { selectedPriority = priority },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPriority == priority)
                                    priority.toColor()
                                else
                                    priority.toColor().copy(alpha = 0.2f),
                                contentColor = if (selectedPriority == priority)
                                    Color.White
                                else
                                    priority.toColor()
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                priority.toDisplayName(),
                                fontWeight = if (selectedPriority == priority)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Przypomnienie
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
                            visible = hasReminder && selectedDate != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Typ przypomnienia
                                var expandedType by remember { mutableStateOf(false) }
                                val reminderTypeOptions = listOf(
                                    ReminderType.MINUTES_BEFORE to "Minuty przed",
                                    ReminderType.CUSTOM_TIME to "O konkretnej godzinie",
                                    ReminderType.DAYS_BEFORE to "Dni przed"
                                )

                                ExposedDropdownMenuBox(
                                    expanded = expandedType,
                                    onExpandedChange = { expandedType = it }
                                ) {
                                    OutlinedTextField(
                                        value = reminderTypeOptions.find { it.first == reminderType }?.second ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Typ") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedType) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedType,
                                        onDismissRequest = { expandedType = false }
                                    ) {
                                        reminderTypeOptions.forEach { (type, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    reminderType = type
                                                    expandedType = false
                                                }
                                            )
                                        }
                                    }
                                }

                                when (reminderType) {
                                    ReminderType.MINUTES_BEFORE -> {
                                        var expandedMinutes by remember { mutableStateOf(false) }
                                        val minutesOptions = listOf(
                                            5 to "5 minut przed",
                                            15 to "15 minut przed",
                                            30 to "30 minut przed",
                                            60 to "1 godzinę przed",
                                            1440 to "1 dzień przed"
                                        )

                                        ExposedDropdownMenuBox(
                                            expanded = expandedMinutes,
                                            onExpandedChange = { expandedMinutes = it }
                                        ) {
                                            OutlinedTextField(
                                                value = minutesOptions.find { it.first == reminderMinutesBefore }?.second ?: "",
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMinutes) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor()
                                            )

                                            ExposedDropdownMenu(
                                                expanded = expandedMinutes,
                                                onDismissRequest = { expandedMinutes = false }
                                            ) {
                                                minutesOptions.forEach { (minutes, label) ->
                                                    DropdownMenuItem(
                                                        text = { Text(label) },
                                                        onClick = {
                                                            reminderMinutesBefore = minutes
                                                            expandedMinutes = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    ReminderType.CUSTOM_TIME -> {
                                        OutlinedButton(
                                            onClick = { showReminderTimePicker = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Schedule, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Przypomnienie o: $reminderCustomTime")
                                        }
                                    }

                                    ReminderType.DAYS_BEFORE -> {
                                        var expandedDays by remember { mutableStateOf(false) }
                                        val daysOptions = (1..7).map { it to "$it dni przed" }

                                        ExposedDropdownMenuBox(
                                            expanded = expandedDays,
                                            onExpandedChange = { expandedDays = it }
                                        ) {
                                            OutlinedTextField(
                                                value = "$reminderCustomDays dni przed",
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDays) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor()
                                            )

                                            ExposedDropdownMenu(
                                                expanded = expandedDays,
                                                onDismissRequest = { expandedDays = false }
                                            ) {
                                                daysOptions.forEach { (days, label) ->
                                                    DropdownMenuItem(
                                                        text = { Text(label) },
                                                        onClick = {
                                                            reminderCustomDays = days
                                                            expandedDays = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { showReminderTimePicker = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Schedule, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("O godzinie: $reminderCustomTime")
                                        }
                                    }
                                    // --- POCZĄTEK POPRAWKI ---
                                    // Dodajemy gałąź `else`, która obsługuje przypadek `null`.
                                    // W tym miejscu po prostu nic nie robimy.
                                    else ->{}
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Przyciski
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
                            if (title.isNotBlank()) {
                                onSave(
                                    task.copy(
                                        title = title,
                                        description = description,
                                        date = selectedDate,
                                        time = selectedTime,
                                        priority = selectedPriority,
                                        hasReminder = hasReminder && selectedDate != null,
                                        reminderType = reminderType,
                                        reminderMinutesBefore = reminderMinutesBefore,
                                        reminderCustomTime = reminderCustomTime,
                                        reminderCustomDays = reminderCustomDays
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Zapisz", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.let {
                try {
                    LocalDate.parse(it).toEpochDay() * 24 * 60 * 60 * 1000
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000)).toString()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker (czas zadania)
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime?.let {
                try {
                    LocalTime.parse(it).hour
                } catch (e: Exception) {
                    12
                }
            } ?: 12,
            initialMinute = selectedTime?.let {
                try {
                    LocalTime.parse(it).minute
                } catch (e: Exception) {
                    0
                }
            } ?: 0
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Anuluj")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    // TimePicker (przypomnienie)
    if (showReminderTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = try {
                LocalTime.parse(reminderCustomTime).hour
            } catch (e: Exception) {
                9
            },
            initialMinute = try {
                LocalTime.parse(reminderCustomTime).minute
            } catch (e: Exception) {
                0
            }
        )

        AlertDialog(
            onDismissRequest = { showReminderTimePicker = false },
            title = { Text("Godzina przypomnienia") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderCustomTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showReminderTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTimePicker = false }) {
                    Text("Anuluj")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń zadanie") },
            text = { Text("Czy na pewno chcesz usunąć to zadanie?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(task)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
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