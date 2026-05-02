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
import androidx.compose.ui.draw.shadow
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
    onDeleteTask: ((Task) -> Unit)? = null,
    showDate: Boolean = true,
    modifier: Modifier = Modifier
) {
    val priorityColor = task.priority.toColor()
    val isOverdue = task.date != null && !task.isCompleted &&
            LocalDate.parse(task.date).isBefore(LocalDate.now())
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteTask?.invoke(task)
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (!task.isCompleted) onTaskCheckedChange(task.id)
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { it * 0.3f }
    )

    SwipeToDismissBox(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF2E7D32)
                            SwipeToDismissBoxValue.EndToStart -> Color(0xFFC62828)
                            else -> Color.Transparent
                        },
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Ukończ",
                        tint = Color.White
                    )
                    SwipeToDismissBoxValue.EndToStart -> Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Usuń",
                        tint = Color.White
                    )
                    else -> Unit
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = onDeleteTask != null
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(priorityColor.copy(alpha = 0.85f))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onTaskCheckedChange(task.id) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = priorityColor,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (task.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

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

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = priorityColor.copy(alpha = 0.18f),
                            modifier = Modifier.shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(8.dp),
                                ambientColor = priorityColor.copy(alpha = 0.45f),
                                spotColor = priorityColor.copy(alpha = 0.45f)
                            )
                        ) {
                            Text(
                                text = task.priority.toDisplayName(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

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
}