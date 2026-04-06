// Plik: ui/dialogs/AddGoalSheet.kt
package com.tasker.chronos.ui.dialogs

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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Goal
import com.tasker.chronos.data.models.MiniGoal
import com.tasker.chronos.ui.components.*
import com.tasker.chronos.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (Goal) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") } // ✅ DODANE
    var miniGoals by remember { mutableStateOf(listOf<MiniGoal>()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var newMiniGoalTitle by remember { mutableStateOf("") }
    var newMiniGoalDate by remember { mutableStateOf<String?>(null) }
    var showMiniGoalDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
// ✅ NOWE: Cykliczne przypomnienia
    var hasPeriodicReminder by remember { mutableStateOf(false) }
    var reminderIntervalWeeks by remember { mutableStateOf(4) }
    var reminderTime by remember { mutableStateOf("09:00") }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },

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
                                GoalGold.copy(alpha = 0.8f),
                                GoalOrange.copy(alpha = 0.6f)
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
                        Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Text(
                        "Nowy cel",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                    label = "Tytuł celu",
                    icon = Icons.Default.Flag
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

                // Data końcowa
                GlassButton(
                    onClick = { showDatePicker = true },
                    icon = Icons.Default.CalendarToday,
                    text = endDate?.let {
                        try {
                            LocalDate.parse(it).format(
                                java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy")
                            )
                        } catch (e: Exception) {
                            "Wybierz datę końcową"
                        }
                    } ?: "Wybierz datę końcową (opcjonalnie)",
                    modifier = Modifier.fillMaxWidth()
                )

                // Mini-cele
                Text(
                    "Mini-cele",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Lista mini-celów
                miniGoals.forEach { miniGoal ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = miniGoal.title,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                miniGoal.date?.let { date ->
                                    Text(
                                        text = "📅 ${formatDate(date)}",
                                        fontSize = 12.sp,
                                        color = GoalOrange
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    miniGoals = miniGoals.filter { it.id != miniGoal.id }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Usuń",
                                    tint = PriorityHigh
                                )
                            }
                        }
                    }
                }

                // Dodawanie mini-celu
                GlassSurface {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newMiniGoalTitle,
                                onValueChange = { newMiniGoalTitle = it },
                                label = { Text("Nowy mini-cel") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            IconButton(onClick = { showMiniGoalDatePicker = true }) {
                                Icon(
                                    if (newMiniGoalDate != null) Icons.Default.Event else Icons.Default.CalendarToday,
                                    contentDescription = "Data",
                                    tint = if (newMiniGoalDate != null) GoalOrange
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledIconButton(
                                onClick = {
                                    if (newMiniGoalTitle.isNotBlank()) {
                                        // ✅ WALIDACJA: Data mini-celu nie może być późniejsza niż deadline celu
                                        if (newMiniGoalDate != null && endDate != null) {
                                            try {
                                                val miniDate = LocalDate.parse(newMiniGoalDate)
                                                val goalDeadline = LocalDate.parse(endDate)

                                                // ❌ STARY KOD: if (miniDate.isAfter(goalDeadline)) return@FilledIconButton

                                                // ✅ NOWY KOD: Pokaż komunikat
                                                if (miniDate.isAfter(goalDeadline)) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "⚠️ Data mini-celu nie może być późniejsza niż ${
                                                                formatDate(
                                                                    endDate!!
                                                                )
                                                            }",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                    return@FilledIconButton
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e(
                                                    "AddGoalSheet",
                                                    "Błąd parsowania dat: ${e.message}"
                                                )
                                            }
                                        }

                                        miniGoals = miniGoals + MiniGoal(
                                            id = UUID.randomUUID().toString(),
                                            title = newMiniGoalTitle,
                                            date = newMiniGoalDate
                                        )
                                        newMiniGoalTitle = ""
                                        newMiniGoalDate = null
                                    }
                                },
                                enabled = newMiniGoalTitle.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Dodaj")
                            }
                        }

                        if (newMiniGoalDate != null) {
                            Text(
                                text = "Data: ${formatDate(newMiniGoalDate!!)}",
                                fontSize = 12.sp,
                                color = GoalOrange,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            // ✅ DODAJ walidację wizualną
                            val isDateInvalid = remember(newMiniGoalDate, endDate) {
                                if (endDate != null) {
                                    try {
                                        val miniDate = LocalDate.parse(newMiniGoalDate)
                                        val goalDeadline = LocalDate.parse(endDate)
                                        miniDate.isAfter(goalDeadline)
                                    } catch (e: Exception) {
                                        false
                                    }
                                } else {
                                    false
                                }
                            }

                            if (isDateInvalid) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = PriorityHigh,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Data mini-celu nie może być późniejsza niż ${
                                            formatDate(
                                                endDate!!
                                            )
                                        }",
                                        fontSize = 11.sp,
                                        color = PriorityHigh,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                }
                GlassSurface {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Note,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Notatki", fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Dodaj notatki...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 8,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                // ✅ Cykliczne przypomnienia
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                                Text("Cykliczne przypomnienia", fontWeight = FontWeight.Medium)
                            }
                            Switch(checked = hasPeriodicReminder, onCheckedChange = { hasPeriodicReminder = it })
                        }

                        if (hasPeriodicReminder) {
                            Text("Częstotliwość:", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)

                            val intervals = listOf(1 to "1 tydzień", 2 to "2 tygodnie",
                                4 to "1 miesiąc", 8 to "2 miesiące",
                                12 to "3 miesiące", 26 to "6 miesięcy")

                            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                                modifier = Modifier.height(120.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(intervals.size) { i ->
                                    val (weeks, label) = intervals[i]
                                    FilterChip(
                                        selected = reminderIntervalWeeks == weeks,
                                        onClick = { reminderIntervalWeeks = weeks },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showReminderTimePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Godzina: $reminderTime")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }


            // === BOTTOM BUTTONS ===
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
                                try {
                                    onConfirm(
                                        Goal(
                                            id = UUID.randomUUID().toString(),
                                            title = title,
                                            description = description,
                                            endDate = endDate,
                                            miniGoals = miniGoals,
                                            notes = notes, // ✅ Zmieniono z "" na notes
                                            createdAt = java.time.LocalDateTime.now().toString(),
                                            hasPeriodicReminder = hasPeriodicReminder,
                                            reminderIntervalWeeks = reminderIntervalWeeks,
                                            reminderTime = reminderTime
                                        )
                                    )
                                    onDismiss()
                                } catch (e: Exception) {
                                    android.util.Log.e("AddGoalSheet", "Błąd: ${e.message}")
                                }
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
    if (showReminderTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderTime.split(":")[0].toIntOrNull() ?: 9,
            initialMinute = reminderTime.split(":")[1].toIntOrNull() ?: 0,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showReminderTimePicker = false },
            title = { Text("Godzina przypomnienia") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = String.format(
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    showReminderTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTimePicker = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.let {
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
                            endDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000)).toString()
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

    if (showMiniGoalDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = newMiniGoalDate?.let {
                try {
                    LocalDate.parse(it).toEpochDay() * 24 * 60 * 60 * 1000
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showMiniGoalDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            newMiniGoalDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000)).toString()
                        }
                        showMiniGoalDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMiniGoalDatePicker = false }) {
                    Text("Anuluj")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"))
    } catch (e: Exception) {
        dateString
    }
}