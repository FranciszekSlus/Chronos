package com.tasker.chronos.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tasker.chronos.data.models.HabitFrequency
import java.time.DayOfWeek
import java.time.LocalTime

// Komponent 1: FrequencySelector (pozostaje bez zmian)
@Composable
fun FrequencySelector(
    selectedFrequency: HabitFrequency,
    onFrequencySelected: (HabitFrequency) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HabitFrequency.values().forEach { frequency ->
            FilterChip(
                selected = selectedFrequency == frequency,
                onClick = { onFrequencySelected(frequency) },
                label = { Text(frequency.name) }
            )
        }
    }
}

// --- NOWY, LEPSZY KOMPONENT WYBORU DNIA ---
@Composable
private fun CustomDayChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp), // Zapewnia odpowiednią wysokość
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    }
}


// Komponent 2: DayOfWeekSelector (używa nowego CustomDayChip)
@Composable
fun DayOfWeekSelector(
    selectedDays: Set<String>,
    onDaySelected: (String) -> Unit
) {
    val polishDays = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
    val originalDays = DayOfWeek.values().map { it.name }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp) // Równomierne małe odstępy
    ) {
        polishDays.forEachIndexed { index, dayLabel ->
            val dayName = originalDays[index]
            CustomDayChip(
                text = dayLabel,
                isSelected = selectedDays.contains(dayName),
                onClick = { onDaySelected(dayName) },
                modifier = Modifier.weight(1f) // Każdy zajmuje tyle samo miejsca
            )
        }
    }
}

// Komponent 3: MonthlyDateSelector (używa nowego CustomDayChip)
@Composable
fun MonthlyDateSelector(
    selectedDates: Set<Int>,
    onDateSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..31).toList().chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { day ->
                    CustomDayChip(
                        text = day.toString(),
                        isSelected = selectedDates.contains(day),
                        onClick = { onDateSelected(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Wypełnienie pustych miejsc w ostatnim rzędzie
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// Komponent 4: TimePickerDialog (pozostaje bez zmian)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    initialTime: LocalTime
) {
    val timeState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Wybierz godzinę") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePicker(state = timeState)
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(LocalTime.of(timeState.hour, timeState.minute))
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Anuluj")
            }
        }
    )
}