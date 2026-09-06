// Plik: ui/components/AddCustomEventDialog.kt - ZAKTUALIZOWANY
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter


enum class ReminderUnit(val label: String, val multiplier: Int) {
    MINUTES("minut", 1),
    HOURS("godzin", 60),
    DAYS("dni", 1440)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomEventDialog(
    date: String,
    onDismiss: () -> Unit,
    lastEventEndTime: String? = null,
    onConfirm: (CustomEvent) -> Unit,
    showMonthViewToggle: Boolean = true
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val startDate = date  // ✅ Data rozpoczęcia = wybrany dzień (bez UI)
    var endDate by remember { mutableStateOf<String?>(null) }
    var isMultiDay by remember { mutableStateOf(false) }
    var showInMonthView by remember { mutableStateOf(!showMonthViewToggle) }  // ✅ false dla DayView, true dla MonthView

    // ✅ POPRAWIONE: Automatyczna godzina na podstawie lastEventEndTime
    // ✅ POPRAWIONE: Automatyczna godzina na podstawie lastEventEndTime
    val defaultStartTime = lastEventEndTime ?: "08:00"
    val defaultEndTime = remember(defaultStartTime) {
        try {
            val parts = defaultStartTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            // Dodaj 1 godzinę do czasu zakończenia poprzedniego wydarzenia
            val newHour = (hour + 1) % 24
            String.format("%02d:%02d", newHour, minute)
        } catch (e: Exception) {
            "09:00"
        }
    }

    var startTime by remember { mutableStateOf(defaultStartTime) }  // ✅ Użyj obliczonej wartości
    var endTime by remember { mutableStateOf(defaultEndTime) }

    var selectedColor by remember { mutableStateOf(0xFF2196F3L) }
    var hasCustomReminder by remember { mutableStateOf(false) }  // ✅ Zmieniona nazwa
    var reminderValue by remember { mutableStateOf(15) }
    var reminderUnit by remember { mutableStateOf(ReminderUnit.MINUTES) }  // ✅ NOWE

    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj Wydarzenie") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)  // ✅ WAŻNE: Maksymalna wysokość
                    .verticalScroll(rememberScrollState())  // ✅ SCROLL
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
                    label = { Text("Opis (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Divider()

                // ✅ Checkbox wielodniowe wydarzenie
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

                // ✅ Data zakończenia (tylko jeśli wielodniowe)
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

                // ✅ Kolor wydarzenia - kompaktowy poziomy scroll
                Text("Kolor wydarzenia:", fontWeight = FontWeight.Medium, fontSize = 14.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
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

                // ✅ Przypomnienie - w Card dla lepszej widoczności
                // ✅ Info o powiadomieniu w momencie rozpoczęcia
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

// ✅ Dodatkowe przypomnienie (opcjonalne)

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
                            checked = hasCustomReminder,
                            onCheckedChange = { hasCustomReminder = it }
                        )
                    }
                }

                // ✅ Checkbox widoczności - TYLKO jeśli showMonthViewToggle = true
                if (showMonthViewToggle) {
                    Divider()

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
                    }
                }

                if (hasCustomReminder) {
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

                        onConfirm(
                            CustomEvent(
                                title = title,
                                description = description,
                                date = startDate,
                                endDate = finalEndDate,
                                startTime = startTime,
                                endTime = endTime,
                                color = selectedColor,
                                hasReminder = hasCustomReminder,
                                reminderMinutesBefore = if (hasCustomReminder) {
                                    reminderValue * reminderUnit.multiplier
                                } else {
                                    0
                                },
                                showInMonthView = showInMonthView  // ✅ DODAJ TO
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && (!isMultiDay || endDate != null)
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )

    // ✅ Picker daty zakończenia
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
}
fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy"))
    } catch (e: Exception) {
        dateString
    }
}


// ✅ NOWY KOMPONENT: ColorPicker
@Composable
fun ColorPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit
) {
    val colors = listOf(
        0xFF2196F3L to "Niebieski",
        0xFF4CAF50L to "Zielony",
        0xFFFFC107L to "Żółty",
        0xFFFF9800L to "Pomarańczowy",
        0xFFF44336L to "Czerwony",
        0xFFE91E63L to "Różowy",
        0xFF9C27B0L to "Fioletowy",
        0xFF00BCD4L to "Cyjan",
        0xFF795548L to "Brązowy",
        0xFF607D8BL to "Szary"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(120.dp)
    ) {
        items(colors) { (color, name) ->
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
                    .clickable { onColorSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (color == selectedColor) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Wybrany",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = com.tasker.chronos.utils.DateMillis.parseToUtcMillisOrNow(selectedDate)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(
                            com.tasker.chronos.utils.DateMillis.utcMillisToLocalDate(millis).toString()
                        )
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = selectedTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val time = String.format(
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onTimeSelected(time)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}