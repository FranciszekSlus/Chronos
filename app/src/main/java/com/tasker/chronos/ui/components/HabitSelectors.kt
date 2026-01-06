// Plik: ui/components/HabitSelectors.kt
package com.tasker.chronos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.HabitFrequency // <-- WAŻNY IMPORT

// --- POCZĄTEK POPRAWKI ---

// USUNIĘTO: enum class FrequencyType { ... } - nie jest już potrzebny

@Composable
fun AnimatedFrequencyButton(
    type: HabitFrequency, // Zmieniono typ z FrequencyType na HabitFrequency
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // Logika dostosowana do HabitFrequency
    val icon = when (type) {
        HabitFrequency.CODZIENNY -> Icons.Default.Today
        HabitFrequency.TYGODNIOWY -> Icons.Default.CalendarViewWeek
        HabitFrequency.MIESIECZNY -> Icons.Default.CalendarMonth
    }

    val label = when (type) {
        HabitFrequency.CODZIENNY -> "Codziennie"
        HabitFrequency.TYGODNIOWY -> "Tydzień"
        HabitFrequency.MIESIECZNY -> "Miesiąc"
    }

    // --- KONIEC POPRAWKI ---

    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                Color(0xFF4CAF50)
            else
                Color(0xFF4CAF50).copy(alpha = 0.2f),
            contentColor = if (isSelected) Color.White else Color(0xFF4CAF50)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

// Reszta pliku (WeekDaySelector, MonthDaySelector, DayChip) pozostaje bez zmian

@Composable
fun WeekDaySelector(
    selectedDays: Set<String>,
    onDaysChanged: (Set<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Wybierz dni tygodnia:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val days = listOf(
                "MONDAY" to "Pn",
                "TUESDAY" to "Wt",
                "WEDNESDAY" to "Śr",
                "THURSDAY" to "Cz"
            )

            days.forEach { (day, label) ->
                DayChip(
                    label = label,
                    isSelected = selectedDays.contains(day),
                    onClick = {
                        onDaysChanged(
                            if (selectedDays.contains(day))
                                selectedDays - day
                            else
                                selectedDays + day
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val days = listOf(
                "FRIDAY" to "Pt",
                "SATURDAY" to "So",
                "SUNDAY" to "Nd"
            )

            days.forEach { (day, label) ->
                DayChip(
                    label = label,
                    isSelected = selectedDays.contains(day),
                    onClick = {
                        onDaysChanged(
                            if (selectedDays.contains(day))
                                selectedDays - day
                            else
                                selectedDays + day
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MonthDaySelector(
    selectedDays: Set<Int>,
    onDaysChanged: (Set<Int>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Wybierz dni miesiąca:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..31).chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    week.forEach { day ->
                        DayChip(
                            label = day.toString(),
                            isSelected = selectedDays.contains(day),
                            onClick = {
                                onDaysChanged(
                                    if (selectedDays.contains(day))
                                        selectedDays - day
                                    else
                                        selectedDays + day
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DayChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Surface(
        modifier = modifier
            .height(44.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isSelected) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF4CAF50)
            )
        }
    }
}