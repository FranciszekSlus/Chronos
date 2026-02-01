// Plik: ui/components/EditCustomEventDialog.kt - ZAKTUALIZOWANY
package com.tasker.chronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.tasker.chronos.data.models.CustomEvent
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomEventDialog(
    event: CustomEvent,
    onDismiss: () -> Unit,
    onSave: (CustomEvent) -> Unit,
    onDelete: (CustomEvent) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }

    var startDate by remember { mutableStateOf(event.date) }
    var endDate by remember { mutableStateOf(event.endDate) }
    var isMultiDay by remember { mutableStateOf(event.isMultiDay()) }  // ✅ UŻYJ FUNKCJI

    var startTime by remember { mutableStateOf(event.startTime) }
    var endTime by remember { mutableStateOf(event.endTime) }

    var selectedColor by remember { mutableStateOf(event.color) }  // ✅ DODANE
    var hasReminder by remember { mutableStateOf(event.hasReminder) }
    var reminderMinutes by remember { mutableStateOf(event.reminderMinutesBefore) }
    var reminderUnit by remember { mutableStateOf(ReminderUnit.MINUTES) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }  // ✅ NOWE
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var reminderValue by remember { mutableStateOf(15) }
    var showInMonthView by remember { mutableStateOf(event.showInMonthView) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj Wydarzenie") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)  // ✅ DODAJ maksymalną wysokość
                    .verticalScroll(rememberScrollState())  // ✅ DODAJ scroll
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tytuł
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Opis
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Divider()

                // ✅ NOWE: Checkbox wielodniowe wydarzenie
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isMultiDay,
                        onCheckedChange = {
                            isMultiDay = it
                            if (!it) endDate = null
                        }
                    )
                    Text("Wydarzenie wielodniowe")
                }

                // Data rozpoczęcia
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Data rozpoczęcia: ${formatDate(startDate)}")
                }

                // ✅ NOWE: Data zakończenia
                if (isMultiDay) {
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Data zakończenia: ${endDate?.let { formatDate(it) } ?: "Wybierz"}")
                    }
                }

                // Godzina rozpoczęcia
                OutlinedButton(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Godzina rozpoczęcia: $startTime")
                }

                // Godzina zakończenia
                OutlinedButton(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Godzina zakończenia: $endTime")
                }

                Divider()

                // ✅ NOWE: Wybór koloru
                // ✅ Kolor jako kompaktowy selector zamiast dużej siatki
                Text("Kolor wydarzenia:", fontWeight = FontWeight.Medium, fontSize = 14.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),  // ✅ Poziomy scroll
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0xFF2196F3L to "Niebieski",
                        0xFF4CAF50L to "Zielony",
                        0xFFFFC107L to "Żółty",
                        0xFFFF9800L to "Pomarańczowy",
                        0xFFF44336L to "Czerwony",
                        0xFFE91E63L to "Różowy",
                        0xFF9C27B0L to "Fioletowy",
                        0xFF00BCD4L to "Cyjan"
                    ).forEach { (color, name) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = if (color == selectedColor) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
                Divider()

// ✅ NOWE: Info o powiadomieniu w momencie rozpoczęcia (tak jak w Add)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Powiadomienie w momencie rozpoczęcia",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ ZNAJDŹ I ZMIEŃ showInMonthViewState → showInMonthView
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Widoczny w widoku miesiąca",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            "Pokaż to wydarzenie w kalendarzu miesięcznym",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showInMonthView,
                        onCheckedChange = { showInMonthView = it }
                    )
                }

                Divider()

// ✅ NOWE: Dodatkowe przypomnienie (opcjonalne)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dodatkowe przypomnienie", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = hasReminder,
                            onCheckedChange = { hasReminder = it }
                        )
                    }
                }

                if (hasReminder) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Przypomnij przed:", fontSize = 12.sp, fontWeight = FontWeight.Medium)

                        // ✅ Pole do wpisania wartości
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = reminderValue.toString(),
                                onValueChange = {
                                    reminderValue = it.toIntOrNull()?.coerceIn(1, 999) ?: 15
                                },
                                label = { Text("Ilość") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // ✅ Dropdown z jednostkami
                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = reminderUnit.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Jednostka") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier.menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    ReminderUnit.values().forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit.label) },
                                            onClick = {
                                                reminderUnit = unit
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ✅ Podgląd
                        Text(
                            text = "Powiadomienie: ${reminderValue * reminderUnit.multiplier} minut przed",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }



                // Przycisk usuwania
                // ✅ Przycisk usuwania - info o wielodniowym
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Usuń wydarzenie")
                        if (event.isMultiDay()) {
                            Text(
                                "Usuwa ze wszystkich dni",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        // ✅ Walidacja: dla wielodniowych, endDate musi być >= startDate
                        val finalEndDate = if (isMultiDay && endDate != null) {
                            val start = LocalDate.parse(startDate)
                            val end = LocalDate.parse(endDate)
                            if (end.isBefore(start)) startDate else endDate
                        } else null

                        onSave(
                            event.copy(
                                title = title,
                                description = description,
                                date = startDate,
                                endDate = finalEndDate,  // ✅ DODANE
                                startTime = startTime,
                                endTime = endTime,
                                color = selectedColor,  // ✅ DODANE
                                hasReminder = hasReminder,
                                reminderMinutesBefore = reminderMinutes,
                                showInMonthView = showInMonthView
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && (!isMultiDay || endDate != null)
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )

    // Picker daty rozpoczęcia
    if (showStartDatePicker) {
        DatePickerDialog(
            selectedDate = startDate,
            onDateSelected = { newDate ->
                startDate = newDate
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // ✅ NOWY: Picker daty zakończenia
    if (showEndDatePicker) {
        DatePickerDialog(
            selectedDate = endDate ?: startDate,
            onDateSelected = { newDate ->
                endDate = newDate
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    // Picker godziny rozpoczęcia
    if (showStartTimePicker) {
        TimePickerDialog(
            selectedTime = startTime,
            onTimeSelected = { newTime ->
                startTime = newTime
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    // Picker godziny zakończenia
    if (showEndTimePicker) {
        TimePickerDialog(
            selectedTime = endTime,
            onTimeSelected = { newTime ->
                endTime = newTime
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }

    // Dialog potwierdzenia usunięcia
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Usuń wydarzenie") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Czy na pewno chcesz usunąć to wydarzenie?")

                    if (event.isMultiDay()) {
                        val days = event.getAllDates().size
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Wydarzenie trwa $days dni i zostanie usunięte ze wszystkich",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Text(
                        "Tej operacji nie można cofnąć.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        onDelete(event)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Anuluj")
                }
            }
        )

    }
}