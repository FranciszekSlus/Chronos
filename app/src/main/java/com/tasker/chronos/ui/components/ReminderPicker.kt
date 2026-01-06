package com.tasker.chronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tasker.chronos.data.models.TaskReminderUnit
import com.tasker.chronos.data.models.TaskReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPicker(
    reminders: List<TaskReminder>,
    onRemindersChange: (List<TaskReminder>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Lista istniejących przypomnień
        reminders.forEachIndexed { index, reminder ->
            ReminderItem(
                reminder = reminder,
                onReminderChange = { newReminder ->
                    val updated = reminders.toMutableList()
                    updated[index] = newReminder
                    onRemindersChange(updated)
                },
                onRemove = {
                    val updated = reminders.toMutableList()
                    updated.removeAt(index)
                    onRemindersChange(updated)
                }
            )
            if (index < reminders.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Przycisk dodania nowego przypomnienia
        if (reminders.size < 5) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    val newReminder = TaskReminder(30, TaskReminderUnit.MINUTES)
                    onRemindersChange(reminders + newReminder)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dodaj kolejne przypomnienie", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderItem(
    reminder: TaskReminder,
    onReminderChange: (TaskReminder) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(reminder.value.toString()) }
    val units = TaskReminderUnit.entries.toList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Pole liczby - POPRAWIONE
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    // Pozwól na pusty string (podczas usuwania)
                    if (newValue.isEmpty()) {
                        textValue = ""
                        return@OutlinedTextField
                    }

                    // Tylko cyfry
                    if (newValue.all { it.isDigit() }) {
                        val number = newValue.toIntOrNull()
                        if (number != null && number > 0 && number <= 999) {
                            textValue = newValue
                            onReminderChange(reminder.copy(value = number))
                        }
                    }
                },
                modifier = Modifier.width(70.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Dropdown jednostki czasu
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = reminder.unit.getPolishName(reminder.value),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.Gray
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF2E2E2E))
                ) {
                    units.forEach { unitOption ->
                        DropdownMenuItem(
                            text = { Text(unitOption.displayName, color = Color.White) },
                            onClick = {
                                onReminderChange(reminder.copy(unit = unitOption))
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Przycisk usuwania
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Usuń przypomnienie",
                    tint = Color.White
                )
            }
        }
    }
}