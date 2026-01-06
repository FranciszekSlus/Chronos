// Plik: ui/components/AddEventDialogForSchedule.kt
package com.tasker.chronos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasker.chronos.data.models.CalendarEvent
import com.tasker.chronos.data.models.EventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialogForSchedule(
    selectedDate: String,
    onDismiss: () -> Unit,
    onConfirm: (CalendarEvent) -> Unit
) {
    var eventTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("09") }
    var startMinute by remember { mutableStateOf("00") }
    var endHour by remember { mutableStateOf("10") }
    var endMinute by remember { mutableStateOf("00") }
    var hasReminder by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj wydarzenie", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
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

                // Opis
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Godzina rozpoczęcia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { if (it.length <= 2) startHour = it },
                        label = { Text("Godz.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startMinute,
                        onValueChange = { if (it.length <= 2) startMinute = it },
                        label = { Text("Min.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Text(" - ", modifier = Modifier.padding(top = 16.dp))
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { if (it.length <= 2) endHour = it },
                        label = { Text("Godz.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endMinute,
                        onValueChange = { if (it.length <= 2) endMinute = it },
                        label = { Text("Min.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Przypomnienie
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Przypomnienie")
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (eventTitle.isNotBlank()) {
                        // Połącz tytuł z opisem jeśli istnieje
                        val eventTitleWithDesc = if (description.isNotBlank()) {
                            "$eventTitle - $description"
                        } else {
                            eventTitle
                        }

                        val event = CalendarEvent(
                            date = selectedDate,
                            startTime = "${startHour.padStart(2, '0')}:${startMinute.padStart(2, '0')}",
                            endTime = "${endHour.padStart(2, '0')}:${endMinute.padStart(2, '0')}",
                            title = eventTitleWithDesc,
                            type = EventType.CUSTOM, // POPRAWKA: użyj enum!
                            hasReminder = hasReminder
                        )

                        onConfirm(event)
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
}