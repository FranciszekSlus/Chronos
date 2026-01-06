// Plik: ui/components/AddTaskSheet.kt
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
import androidx.compose.ui.draw.scale
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
import com.tasker.chronos.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit,
    initialDate: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var hasReminder by remember { mutableStateOf(false) }

    var reminderType by remember { mutableStateOf(ReminderType.MINUTES_BEFORE) }
    var reminderMinutesBefore by remember { mutableStateOf(60) }
    var reminderCustomTime by remember { mutableStateOf("09:00") }
    var reminderCustomDays by remember { mutableStateOf(1) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

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
            // === GRADIENT HEADER (STICKY) ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                BluePrimary.copy(alpha = 0.8f),
                                BluePrimaryLight.copy(alpha = 0.6f)
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
                        Icons.Default.Task,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Text(
                        "Nowe zadanie",
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
                // Tytuł
                AnimatedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Tytuł zadania",
                    icon = Icons.Default.Task
                )

                // Opis
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis (opcjonalnie)") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp)
                )

                // Data
                GlassButton(
                    onClick = { showDatePicker = true },
                    icon = Icons.Default.CalendarToday,
                    text = selectedDate?.let {
                        try {
                            LocalDate.parse(it).format(
                                java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy")
                            )
                        } catch (e: Exception) {
                            "Wybierz datę"
                        }
                    } ?: "Wybierz datę (opcjonalnie)",
                    modifier = Modifier.fillMaxWidth()
                )

                // Godzina
                AnimatedVisibility(
                    visible = selectedDate != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    GlassButton(
                        onClick = { showTimePicker = true },
                        icon = Icons.Default.Schedule,
                        text = selectedTime?.let { "O $it" } ?: "Dodaj godzinę",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Priorytet
                Text(
                    "Priorytet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TaskPriority.values().forEach { priority ->
                        AnimatedPriorityChip(
                            priority = priority,
                            isSelected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            modifier = Modifier.weight(1f)
                        )
                    }
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

                        // Opcje przypomnienia
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
                                        label = { Text("Typ przypomnienia") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedType) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(12.dp)
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

                                // Opcje w zależności od typu
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
                                                    .menuAnchor(),
                                                shape = RoundedCornerShape(12.dp)
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
                                        GlassButton(
                                            onClick = { showReminderTimePicker = true },
                                            icon = Icons.Default.Schedule,
                                            text = "Przypomnienie o: $reminderCustomTime",
                                            modifier = Modifier.fillMaxWidth()
                                        )
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
                                                    .menuAnchor(),
                                                shape = RoundedCornerShape(12.dp)
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

                                        Spacer(modifier = Modifier.height(8.dp))

                                        GlassButton(
                                            onClick = { showReminderTimePicker = true },
                                            icon = Icons.Default.Schedule,
                                            text = "O godzinie: $reminderCustomTime",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom buttons
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
                            if (title.isNotBlank()) {
                                onConfirm(
                                    Task(
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
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
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

    // DatePicker Dialog
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

    // TimePicker Dialog (dla czasu zadania)
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

    // TimePicker Dialog (dla przypomnienia)
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
}

@Composable
fun AnimatedPriorityChip(
    priority: TaskPriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                priority.toDisplayName(),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = priority.toColor().copy(alpha = 0.3f),
            selectedLabelColor = priority.toColor()
        ),
        modifier = modifier.scale(scale)
    )
}