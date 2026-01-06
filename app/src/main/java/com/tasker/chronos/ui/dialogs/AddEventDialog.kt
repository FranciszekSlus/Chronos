// Plik: ui/components/AddEventDialog.kt
package com.tasker.chronos.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasker.chronos.data.models.CalendarEvent
import com.tasker.chronos.data.models.EventType
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (CalendarEvent) -> Unit
) {
    var eventTitle by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<String?>(null) }
    var endTime by remember { mutableStateOf<String?>(null) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowe wydarzenie", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)  // ✅ DODAJ maksymalną wysokość
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tytuł
                OutlinedTextField(
                    value = eventTitle,
                    onValueChange = { eventTitle = it },
                    label = { Text("Tytuł") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Godzina rozpoczęcia
                OutlinedButton(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(startTime?.let { "Rozpoczęcie: $it" } ?: "Dodaj godzinę rozpoczęcia")
                }

                // Godzina zakończenia
                if (startTime != null) {
                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(endTime?.let { "Zakończenie: $it" } ?: "Dodaj godzinę zakończenia (opcjonalnie)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (eventTitle.isNotBlank()) {
                        onConfirm(
                            CalendarEvent(
                                title = eventTitle,
                                date = initialDate,
                                startTime = startTime,
                                endTime = endTime,
                                type = EventType.CUSTOM // POPRAWKA: użyj enum!
                            )
                        )
                    }
                },
                enabled = eventTitle.isNotBlank()
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

    // Start Time Picker
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startTime?.let {
                try { LocalTime.parse(it).hour } catch (e: Exception) { 9 }
            } ?: 9,
            initialMinute = startTime?.let {
                try { LocalTime.parse(it).minute } catch (e: Exception) { 0 }
            } ?: 0
        )

        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showStartTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Anuluj")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    // End Time Picker
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = endTime?.let {
                try { LocalTime.parse(it).hour } catch (e: Exception) { 10 }
            } ?: 10,
            initialMinute = endTime?.let {
                try { LocalTime.parse(it).minute } catch (e: Exception) { 0 }
            } ?: 0
        )

        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showEndTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Anuluj")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}