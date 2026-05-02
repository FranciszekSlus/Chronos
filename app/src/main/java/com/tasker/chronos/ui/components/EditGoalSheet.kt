// Plik: ui/components/EditGoalSheet.kt
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Goal
import com.tasker.chronos.data.models.MiniGoal
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalSheet(
    goal: Goal,
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit,
    onDelete: (Goal) -> Unit,
    onToggleMiniGoal: (String) -> Unit,
    onAddMiniGoal: (MiniGoal) -> Unit,
    onDeleteMiniGoal: (String) -> Unit,
    onUpdateMiniGoal: (MiniGoal) -> Unit = {},
    onGoalCompleted: (String) -> Unit = {}
) {
    var title by remember { mutableStateOf(goal.title) }
    var description by remember { mutableStateOf(goal.description) }
    var endDate by remember { mutableStateOf(goal.endDate) }
    var notes by remember { mutableStateOf(goal.notes) }
    var hasPeriodicReminder by remember { mutableStateOf(goal.hasPeriodicReminder) }
    var reminderIntervalWeeks by remember { mutableStateOf(goal.reminderIntervalWeeks) }
    var reminderTime by remember { mutableStateOf(goal.reminderTime) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddMiniGoalDialog by remember { mutableStateOf(false) }

    // Animacja wejścia
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }
    val sheetAppearProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "edit_goal_sheet_appear"
    )

    val progress = goal.getProgress()
    val progressPercentage = goal.getProgressPercentage()

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
                    .graphicsLayer {
                        scaleX = sheetAppearProgress
                        scaleY = sheetAppearProgress
                        translationY = (1f - sheetAppearProgress) * 180f
                    }
            ) {
                // Header z gradientem i postępem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF9800).copy(alpha = 0.3f),
                                    Color(0xFFFFEB3B).copy(alpha = 0.2f)
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
                                    "Edytuj cel",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (goal.isCompleted()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Ukończone! 🎉",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }

                            // Przycisk usuwania
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

                        // Pasek postępu
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Postęp",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    "$progressPercentage%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }

                            AnimatedProgressBar(progress = progress)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tytuł
                AnimatedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Tytuł celu",
                    icon = Icons.Default.EmojiEvents
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Opis
                AnimatedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Opis",
                    icon = Icons.Default.Description,

                )

                Spacer(modifier = Modifier.height(16.dp))

                // Data końcowa
                GlassButton(
                    onClick = { showDatePicker = true },
                    icon = Icons.Default.CalendarToday,
                    text = endDate?.let {
                        try {
                            LocalDate.parse(it).toString()
                        } catch (e: Exception) {
                            "Data końcowa"
                        }
                    } ?: "Dodaj datę końcową",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Mini-cele
                // ✅ NOWE: Mini-cele - podziel na Z DATĄ i BEZ DATY
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

                            // ✅ NOWY KOD - wszystkie mini-cele razem
                            val completedCount = goal.miniGoals.count { it.isCompleted }

                            Text(
                                "Mini-cele ($completedCount/${goal.miniGoals.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            IconButton(
                                onClick = { showAddMiniGoalDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Dodaj",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (goal.miniGoals.isEmpty()) {
                            Text(
                                "Brak mini-celów. Dodaj pierwszy!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            // ✅ SEKCJA 1: Mini-cele Z DATĄ (jednorazowe)
                            if (goal.miniGoals.isEmpty()) {
                                Text(
                                    "Brak mini-celów. Dodaj pierwszy!",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                // ✅ WSZYSTKIE mini-cele razem (bez podziału)
                                goal.miniGoals.forEach { miniGoal ->
                                    AnimatedMiniGoalItem(
                                        miniGoal = miniGoal,
                                        goal = goal,
                                        onToggle = { onToggleMiniGoal(miniGoal.id) },  // ✅ Jeden callback dla wszystkich
                                        onDelete = { onDeleteMiniGoal(miniGoal.id) },
                                        onUpdateMiniGoal = onUpdateMiniGoal
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Notatki
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



                // Przyciski akcji
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

                    // ✅ ZNAJDŹ TEN BLOK (około linii 330):
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                // ✅ DODAJ SPRAWDZENIE PRZED onSave:
                                val wasPreviouslyIncomplete = !goal.isCompleted()
                                val updatedGoal = goal.copy(
                                    title = title,
                                    description = description,
                                    endDate = endDate,
                                    notes = notes,
                                    hasPeriodicReminder = hasPeriodicReminder,    // ✅ DODAJ
                                    reminderIntervalWeeks = reminderIntervalWeeks, // ✅ DODAJ
                                    reminderTime = reminderTime
                                )
                                val isNowComplete = updatedGoal.isCompleted()

                                // ✅ WYWOŁAJ TOAST jeśli cel właśnie ukończony
                                if (wasPreviouslyIncomplete && isNowComplete) {
                                    onGoalCompleted(updatedGoal.title)
                                }

                                onSave(updatedGoal)
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

    // DatePicker
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

    // Add MiniGoal Dialog
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (miniGoalTitle.isNotBlank()) {
                            onAddMiniGoal(MiniGoal(title = miniGoalTitle))
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

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń cel") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Czy na pewno chcesz usunąć ten cel?")
                    if (progressPercentage > 0) {
                        Text(
                            "Postęp: $progressPercentage% zostanie utracony!",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(goal)
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
    if (showReminderTimePicker) {
        var tempHour by remember { mutableStateOf(reminderTime.split(":")[0].toInt()) }
        var tempMinute by remember { mutableStateOf(reminderTime.split(":")[1].toInt()) }

        AlertDialog(
            onDismissRequest = { showReminderTimePicker = false },
            title = { Text("Godzina przypomnienia") },
            text = {
                val timePickerState = rememberTimePickerState(
                    initialHour = tempHour,
                    initialMinute = tempMinute,
                    is24Hour = true
                )
                LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                    tempHour = timePickerState.hour
                    tempMinute = timePickerState.minute
                }
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = String.format("%02d:%02d", tempHour, tempMinute)
                    showReminderTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTimePicker = false }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
fun AnimatedProgressBar(progress: Float) {
    var animatedProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(progress) {
        var current = 0f
        val target = progress
        while (current < target) {
            current = (current + 0.02f).coerceAtMost(target)
            animatedProgress = current
            delay(15)
        }
    }

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp)),
        color = Color(0xFFFF9800),
        trackColor = Color(0xFFFF9800).copy(alpha = 0.2f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedMiniGoalItem(
    miniGoal: MiniGoal,
    goal: Goal,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onUpdateMiniGoal: (MiniGoal) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var showMiniGoalDatePicker by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val isChecked = miniGoal.isCompleted

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = if (isChecked)
            Color(0xFF4CAF50).copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Checkbox
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isPressed = true
                    onToggle()
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50)
                )
            )

            // ✅ TYLKO JEDEN COLUMN
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = miniGoal.title,
                    style = if (isChecked) {
                        MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    } else {
                        MaterialTheme.typography.bodyMedium
                    }
                )

                // ✅ PRZYCISK DATY
                TextButton(
                    onClick = { showMiniGoalDatePicker = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = miniGoal.date?.let {
                            try {
                                formatDate(it)
                            } catch (e: Exception) {
                                "Dodaj datę"
                            }
                        } ?: "Dodaj datę",
                        fontSize = 12.sp
                    )
                }
            }

            // Przycisk usuń
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Usuń",
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }

    // ✅ DATEPICKER
    if (showMiniGoalDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = miniGoal.date?.let {
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Przycisk usuń datę (jeśli jest data)
                    if (miniGoal.date != null) {
                        TextButton(
                            onClick = {
                                onUpdateMiniGoal(miniGoal.copy(date = null))
                                showMiniGoalDatePicker = false
                            }
                        ) {
                            Text("Usuń datę")
                        }
                    }

                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val newDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000)).toString()
                                onUpdateMiniGoal(miniGoal.copy(date = newDate))
                            }
                            showMiniGoalDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
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


    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }


    // ✅ NOWE: DatePicker dla mini-celu
    if (showMiniGoalDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = miniGoal.date?.let {
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Przycisk usuń datę
                    if (miniGoal.date != null) {
                        TextButton(
                            onClick = {
                                onUpdateMiniGoal(miniGoal.copy(date = null))
                                showMiniGoalDatePicker = false
                            }
                        ) {
                            Text("Usuń datę")
                        }
                    }

                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val newDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000)).toString()

                                // ✅ WALIDACJA: Sprawdź czy nie późniejsza niż deadline
                                goal.endDate?.let { deadline ->
                                    try {
                                        val miniDate = LocalDate.parse(newDate)
                                        val goalDeadline = LocalDate.parse(deadline)

                                        if (miniDate.isAfter(goalDeadline)) {
                                            // ✅ NOWE: Pokaż Toast/Snackbar
                                            android.util.Log.e("EditGoalSheet",
                                                "❌ Data mini-celu ($miniDate) późniejsza niż deadline celu ($goalDeadline)")

                                            // TODO: Tutaj dodaj Snackbar z komunikatem
                                            // "Data mini-celu nie może być późniejsza niż termin głównego celu"

                                            showMiniGoalDatePicker = false
                                            return@TextButton
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("EditGoalSheet", "Błąd walidacji: ${e.message}")
                                    }
                                }

                                onUpdateMiniGoal(miniGoal.copy(date = newDate))
                            }
                            showMiniGoalDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
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
private fun formatDate(dateString: String?): String {
    if (dateString == null) return "Bez daty"

    return try {
        val date = LocalDate.parse(dateString)
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val yesterday = today.minusDays(1)

        when (date) {
            today -> "Dzisiaj"
            tomorrow -> "Jutro"
            yesterday -> "Wczoraj"
            else -> {
                // Format: "15 sty" lub "15 sty 2025" jeśli inny rok
                val formatter = if (date.year == today.year) {
                    java.time.format.DateTimeFormatter.ofPattern("d MMM", java.util.Locale("pl"))
                } else {
                    java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale("pl"))
                }
                date.format(formatter)
            }
        }
    } catch (e: Exception) {
        dateString
    }

}

