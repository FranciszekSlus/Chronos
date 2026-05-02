package com.tasker.chronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Habit
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HabitItem(
    habit: Habit,
    onHabitCheckedChange: (String) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onHabitClick: () -> Unit,
    onHabitLongClick: () -> Unit,
    showConfetti: Boolean = false, // POPRAWKA: Tylko dla tego konkretnego nawyku
    onConfettiShown: () -> Unit = {}
) {
    val today = LocalDate.now()
    val isCompletedToday = habit.completionDates.contains(today.toString())

    val isWeekly = habit.weeklyDays.isNotEmpty()
    val isMonthly = habit.monthlyDates.isNotEmpty()
    val isDaily = !isWeekly && !isMonthly

    val isHabitActiveToday = when {
        isWeekly -> habit.weeklyDays.contains(today.dayOfWeek.name)
        isMonthly -> habit.monthlyDates.any { it.toIntOrNull() == today.dayOfMonth }
        isDaily -> true
        else -> false
    }

    val textDecoration = if (isCompletedToday) TextDecoration.LineThrough else null
    val contentColor = if (isCompletedToday) Color.Gray else Color.Black
    val cardBackgroundBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF1B5E20), Color(0xFF4CAF50))
    )
    val cardAlpha = if (isCompletedToday) 0.7f else 1.0f

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp)
                .clip(MaterialTheme.shapes.large)
                .background(cardBackgroundBrush)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onHabitClick() },
                        onLongPress = { onHabitLongClick() }
                    )
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(Color(0xFF0B3D11).copy(alpha = 0.85f))
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = textDecoration,
                                color = contentColor,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (habit.streak > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🔥",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF6B35)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = habit.streak.toString(),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF6B35)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+${habit.points}pkt",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE8F5E9)
                                    )
                                }
                            }
                        }

                        val frequencyDetailsText = when {
                            isWeekly -> {
                                fun translateDay(day: String): String = when (day) {
                                    "MONDAY" -> "Pon"
                                    "TUESDAY" -> "Wt"
                                    "WEDNESDAY" -> "Śr"
                                    "THURSDAY" -> "Czw"
                                    "FRIDAY" -> "Pt"
                                    "SATURDAY" -> "Sob"
                                    "SUNDAY" -> "Ndz"
                                    else -> ""
                                }
                                val days = habit.weeklyDays.map { translateDay(it) }.joinToString(", ")
                                "Tygodniowo: $days"
                            }
                            isMonthly -> {
                                val dates = habit.monthlyDates.joinToString(", ")
                                "Miesięcznie: $dates"
                            }
                            else -> "Codziennie"
                        }

                        Text(
                            text = frequencyDetailsText,
                            fontSize = 14.sp,
                            color = if (isCompletedToday) contentColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 6.dp),
                            textDecoration = textDecoration
                        )

                        if (habit.hasReminder && habit.reminderTime != null && habit.reminderTime.isNotBlank()) {
                            val formattedTime = try {
                                LocalTime.parse(habit.reminderTime).format(DateTimeFormatter.ofPattern("HH:mm"))
                            } catch (e: Exception) {
                                habit.reminderTime
                            }

                            Text(
                                text = "Przypomnienie o $formattedTime",
                                fontSize = 12.sp,
                                color = if (isCompletedToday) contentColor.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.padding(top = 4.dp),
                                textDecoration = textDecoration
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(
                        checked = isCompletedToday,
                        onCheckedChange = { onHabitCheckedChange(habit.id) },
                        enabled = true,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White.copy(alpha = 0.8f),
                            checkmarkColor = Color(0xFF2E7D32),
                            disabledCheckedColor = Color.White.copy(alpha = 0.5f),
                            disabledUncheckedColor = Color.White.copy(alpha = 0.35f)
                        )
                    )
                }
            }
        }

        // POPRAWKA: Konfetti pokazuje się tylko dla tego konkretnego nawyku
        if (showConfetti) {
            ConfettiAnimation(onAnimationEnd = onConfettiShown)
        }
    }
}


