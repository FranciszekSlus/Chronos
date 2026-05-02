package com.tasker.chronos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.DaySchedule
import com.tasker.chronos.data.models.ScheduleEvent
import com.tasker.chronos.viewmodels.DayScheduleViewModel
import com.tasker.chronos.viewmodels.EventsViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScheduleScreen(
    dayScheduleViewModel: DayScheduleViewModel,
    eventsViewModel: EventsViewModel,
    selectedDate: String,
    onBack: () -> Unit
) {
    val schedules by dayScheduleViewModel.allSchedules.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<DaySchedule?>(null) }
    var showApplyConfirm by remember { mutableStateOf<DaySchedule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schematy dnia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nowy schemat")
            }
        }
    ) { padding ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📅", fontSize = 64.sp)
                    Text(
                        "Brak schematów",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Stwórz schemat by szybko\nwypełniać swoje dni",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
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
                items(schedules, key = { it.id }) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onEdit = { editingSchedule = schedule },
                        onDelete = { dayScheduleViewModel.deleteSchedule(schedule.id) },
                        onApply = { showApplyConfirm = schedule }
                    )
                }
            }
        }
    }

    // Dialog tworzenia nowego schematu
    if (showCreateDialog) {
        CreateEditScheduleDialog(
            schedule = null,
            onDismiss = { showCreateDialog = false },
            onSave = { newSchedule ->
                dayScheduleViewModel.addSchedule(newSchedule)
                showCreateDialog = false
            }
        )
    }

    // Dialog edycji schematu
    if (editingSchedule != null) {
        CreateEditScheduleDialog(
            schedule = editingSchedule,
            onDismiss = { editingSchedule = null },
            onSave = { updatedSchedule ->
                dayScheduleViewModel.updateSchedule(updatedSchedule)
                editingSchedule = null
            }
        )
    }

    // Potwierdzenie zastosowania schematu
    if (showApplyConfirm != null) {
        AlertDialog(
            onDismissRequest = { showApplyConfirm = null },
            title = { Text("Zastosuj schemat") },
            text = {
                Text("Dodać ${showApplyConfirm!!.events.size} wydarzeń ze schematu \"${showApplyConfirm!!.name}\" do dnia $selectedDate?")
            },
            confirmButton = {
                Button(onClick = {
                    dayScheduleViewModel.applyScheduleToDay(
                        showApplyConfirm!!,
                        selectedDate,
                        eventsViewModel
                    )
                    showApplyConfirm = null
                    onBack()
                }) {
                    Text("Zastosuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyConfirm = null }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: DaySchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApply: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${schedule.events.size} wydarzeń",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edytuj",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = onApply,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Zastosuj", fontSize = 12.sp)
                    }
                }
            }

            // Lista wydarzeń w schemacie
            if (expanded && schedule.events.isNotEmpty()) {
                HorizontalDivider()
                schedule.events.forEach { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(event.color))
                        )
                        Text(
                            text = "${event.startTime} - ${event.endTime}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(90.dp)
                        )
                        Text(
                            text = event.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Usuń schemat") },
            text = { Text("Usunąć schemat \"${schedule.name}\"?") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Anuluj") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditScheduleDialog(
    schedule: DaySchedule?,
    onDismiss: () -> Unit,
    onSave: (DaySchedule) -> Unit
) {
    var name by remember { mutableStateOf(schedule?.name ?: "") }
    var events by remember { mutableStateOf(schedule?.events ?: emptyList()) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<ScheduleEvent?>(null) }
    val suggestedStartTime = remember(events) {
        events.maxByOrNull { parseTimeToMinutes(it.endTime) }?.endTime ?: "09:00"
    }
    val suggestedEndTime = remember(suggestedStartTime) {
        addMinutesToTime(suggestedStartTime, 60)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule == null) "Nowy schemat" else "Edytuj schemat") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa schematu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wydarzenia (${events.size})", fontWeight = FontWeight.Medium)
                    TextButton(onClick = { showAddEventDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Dodaj")
                    }
                }

                if (events.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(events, key = { it.id }) { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { editingEvent = event }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(event.color))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(event.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("${event.startTime} - ${event.endTime}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = { events = events.filter { it.id != event.id } },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Usuń",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            DaySchedule(
                                id = schedule?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                events = events,
                                createdAt = schedule?.createdAt ?: java.time.LocalDateTime.now().toString()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Zapisz") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )

    if (showAddEventDialog || editingEvent != null) {
        ScheduleEventDialog(
            event = editingEvent,
            initialStartTime = if (editingEvent == null) suggestedStartTime else null,
            initialEndTime = if (editingEvent == null) suggestedEndTime else null,
            onDismiss = {
                showAddEventDialog = false
                editingEvent = null
            },
            onSave = { newEvent ->
                events = if (editingEvent != null) {
                    events.map { if (it.id == editingEvent!!.id) newEvent else it }
                } else {
                    events + newEvent
                }
                showAddEventDialog = false
                editingEvent = null
            }
        )
    }
}

@Composable
fun ScheduleEventDialog(
    event: ScheduleEvent?,
    initialStartTime: String? = null,
    initialEndTime: String? = null,
    onDismiss: () -> Unit,
    onSave: (ScheduleEvent) -> Unit
) {
    var title by remember { mutableStateOf(event?.title ?: "") }
    var startTime by remember { mutableStateOf(event?.startTime ?: initialStartTime ?: "09:00") }
    var endTime by remember { mutableStateOf(event?.endTime ?: initialEndTime ?: "10:00") }
    var selectedColor by remember { mutableStateOf(event?.color ?: 0xFF2196F3L) }
    var description by remember { mutableStateOf(event?.description ?: "") }

    val colors = listOf(
        0xFF2196F3L to "Niebieski",
        0xFF4CAF50L to "Zielony",
        0xFFFFC107L to "Żółty",
        0xFFFF9800L to "Pomarańczowy",
        0xFFF44336L to "Czerwony",
        0xFF9C27B0L to "Fioletowy"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (event == null) "Dodaj wydarzenie" else "Edytuj wydarzenie") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nazwa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TimePickerRow(
                    label = "Od",
                    selectedTime = startTime,
                    onTimeSelected = { startTime = it }
                )
                TimePickerRow(
                    label = "Do",
                    selectedTime = endTime,
                    onTimeSelected = { endTime = it }
                )

                Text("Kolor:", fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { (color, _) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = if (color == selectedColor) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(ScheduleEvent(
                            id = event?.id ?: UUID.randomUUID().toString(),
                            title = title,
                            startTime = startTime,
                            endTime = endTime,
                            color = selectedColor,
                            description = description
                        ))
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("Zapisz") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerRow(
    label: String,
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val initialHour = selectedTime.substringBefore(":").toIntOrNull() ?: 9
    val initialMinute = selectedTime.substringAfter(":", "00").toIntOrNull() ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    ) {
        OutlinedTextField(
            value = selectedTime,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(String.format("%02d:%02d", timePickerState.hour, timePickerState.minute))
                        showPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Anuluj") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

private fun parseTimeToMinutes(time: String): Int {
    return try {
        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
        localTime.hour * 60 + localTime.minute
    } catch (_: Exception) {
        0
    }
}

private fun addMinutesToTime(time: String, minutesToAdd: Long): String {
    return try {
        LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            .plusMinutes(minutesToAdd)
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        "10:00"
    }
}