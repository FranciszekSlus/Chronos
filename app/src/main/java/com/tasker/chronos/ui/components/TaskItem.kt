// Plik: ui/components/TaskItem.kt
package com.tasker.chronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Task
import com.tasker.chronos.data.models.TaskPriority
import com.tasker.chronos.data.models.toColor
import com.tasker.chronos.data.models.toDisplayName
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TaskItem(
    task: Task,
    onTaskCheckedChange: (String) -> Unit,
    onTaskClick: () -> Unit,
    onTaskLongClick: () -> Unit,
    showDate: Boolean = true,
    modifier: Modifier = Modifier
) {
    val priorityColor = task.priority.toColor()
    val isOverdue = task.date != null && !task.isCompleted &&
            LocalDate.parse(task.date).isBefore(LocalDate.now())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp) // ✅ Minimalna wysokość jak HabitItem
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTaskClick() },
                    onLongPress = { onTaskLongClick() }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp) // ✅ Zaokrąglenie jak HabitItem
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // ✅ Jednolity padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onTaskCheckedChange(task.id) },
                colors = CheckboxDefaults.colors(
                    checkedColor = priorityColor,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(end = 4.dp)
            )

            // Pasek priorytetu (cieńszy, obok checkboxa)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(50.dp)
                    .background(priorityColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Treść zadania
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Tytuł
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy( // ✅ titleMedium
                        fontWeight = FontWeight.SemiBold, // ✅ Pogrubienie jak HabitItem
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Opis (jeśli istnieje)
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(if (task.isCompleted) 0.5f else 1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp)) // ✅ Większy odstęp

                // Meta informacje w boxach (jak HabitItem)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Data i godzina
                    if (showDate && task.date != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOverdue)
                                Color.Red.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "📅",
                                    style = MaterialTheme.typography.labelMedium
                                )

                                val dateText = try {
                                    val date = LocalDate.parse(task.date)
                                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                                    date.format(formatter)
                                } catch (e: Exception) {
                                    task.date
                                }

                                val timeText = task.time?.let {
                                    try {
                                        LocalTime.parse(it).format(DateTimeFormatter.ofPattern("HH:mm"))
                                    } catch (e: Exception) {
                                        it
                                    }
                                }

                                Text(
                                    text = if (timeText != null) "$dateText, $timeText" else dateText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.primary,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Priorytet
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = task.priority.toDisplayName(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    // Reminder
                    if (task.hasReminder) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🔔",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}