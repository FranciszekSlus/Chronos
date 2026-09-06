// Plik: ui/components/EditTaskSheet.kt
package com.tasker.chronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tasker.chronos.data.models.ReminderType
import com.tasker.chronos.data.models.Task
import com.tasker.chronos.data.models.TaskPriority
import com.tasker.chronos.data.models.toColor
import com.tasker.chronos.data.models.toDisplayName
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
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description) }
    var selectedDate by remember(task.id) { mutableStateOf(task.date) }
    var selectedTime by remember(task.id) { mutableStateOf(task.time) }
    var selectedPriority by remember(task.id) { mutableStateOf(task.priority) }
    var hasReminder by remember(task.id) { mutableStateOf(task.hasReminder) }

    var reminderType by remember(task.id) { mutableStateOf(task.reminderType) }
    var reminderMinutesBefore by remember(task.id) { mutableStateOf(task.reminderMinutesBefore) }
    var reminderCustomTime by remember(task.id) { mutableStateOf(task.reminderCustomTime ?: "09:00") }
    var reminderCustomDays by remember(task.id) { mutableStateOf(task.reminderCustomDays ?: 1) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun commitSave() {
        if (title.isBlank()) return
        val updated = task.copy(
            title = title.trim(),
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
        android.util.Log.d("EditTaskSheet", "Zapisz id=${updated.id} title=${updated.title}")
        onSave(updated)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij")
                    }
                    Text(
                        "Edytuj zadanie",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { commitSave() },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Zapisz", fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
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
                                onCheckedChange = { enabled ->
                                    if (enabled && selectedDate == null) {
                                        showDatePicker = true
                                    }
                                    hasReminder = enabled
                                }
                            )
                        }

                        if (hasReminder && selectedDate == null) {
                            Text(
                                "Wybierz datę zadania, żeby zaplanować powiadomienie.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            TextButton(onClick = { showDatePicker = true }) {
                                Text("Wybierz datę")
                            }
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
                        onClick = { commitSave() },
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
    }

    // DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = com.tasker.chronos.utils.DateMillis.parseToUtcMillisOrNow(selectedDate)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = com.tasker.chronos.utils.DateMillis
                                .utcMillisToLocalDate(millis)
                                .toString()
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