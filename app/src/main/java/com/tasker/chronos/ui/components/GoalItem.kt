// Plik: ui/components/GoalItem.kt
package com.tasker.chronos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.Goal
import com.tasker.chronos.data.models.MiniGoal
import com.tasker.chronos.ui.theme.*
import com.tasker.chronos.utils.formatDaysLeft
import java.time.LocalDate

@Composable
fun GoalItem(
    goal: Goal,
    onGoalClick: () -> Unit,
    onGoalLongClick: () -> Unit = {},
    onToggleGoalCompleted: ((String) -> Unit)? = null,
    onToggleMiniGoal: ((String) -> Unit)? = null,
    isExpanded: Boolean = false,
    onExpandToggle: () -> Unit = {}
) {
    val progress = goal.getProgress()
    val progressPercentage = goal.getProgressPercentage()
    val isCompleted = goal.isCompleted()
    val hasMiniGoals = goal.miniGoals.isNotEmpty()

    val daysLeftText = goal.endDate?.let {
        try {
            formatDaysLeft(LocalDate.parse(it))
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted && hasMiniGoals)
                GoalOrange.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // === HEADER (zawsze widoczny) ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox dla celów BEZ mini-celów
                if (!hasMiniGoals && onToggleGoalCompleted != null) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { onToggleGoalCompleted(goal.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GoalOrange
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tytuł
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (isCompleted && hasMiniGoals)
                                    TextDecoration.LineThrough
                                else null
                            ),
                            color = if (isCompleted && hasMiniGoals)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isCompleted && hasMiniGoals) {
                            Text("🎉", fontSize = 16.sp)
                        }
                    }

                    // Pasek postępu (tylko dla celów z mini-celami)
                    if (hasMiniGoals) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = GoalOrange,
                                trackColor = GoalOrange.copy(alpha = 0.2f)
                            )

                            Text(
                                text = "$progressPercentage%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoalOrange
                            )
                        }
                    }

                    // Meta informacje (licznik + data)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        // Data końcowa
                        if (goal.endDate != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (daysLeftText == null)
                                    PriorityHigh.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ) {
                                val dateText = if (daysLeftText != null) {
                                    "📅 Za $daysLeftText"
                                } else {
                                    "⚠️ Termin minął"
                                }

                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (daysLeftText == null) FontWeight.Bold else FontWeight.Medium,
                                    color = if (daysLeftText == null) PriorityHigh else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Ikona expand/collapse
                if (hasMiniGoals) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // === EXPANDED CONTENT ===
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    GoalOrange.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Opis (jeśli istnieje)
                    if (goal.description.isNotBlank()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(color = GoalOrange.copy(alpha = 0.2f))
                    }

                    // Mini-cele
                    if (goal.miniGoals.isNotEmpty()) {
                        Text(
                            "📋 Mini-cele",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        goal.miniGoals.forEach { miniGoal ->
                            MiniGoalCheckboxItem(
                                miniGoal = miniGoal,
                                isCompleted = miniGoal.isCompleted,  // ✅ Prosto - sprawdź flagę
                                onToggle = { onToggleMiniGoal?.invoke(miniGoal.id) },  // ✅ Jeden callback
                                showDate = miniGoal.date != null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Przyciski
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (goal.notes.isNotBlank()) {
                            OutlinedButton(
                                onClick = onGoalClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.StickyNote2,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Notatki", fontWeight = FontWeight.Medium)
                            }
                        }

                        Button(
                            onClick = onGoalClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoalOrange
                            )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edytuj", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniGoalCheckboxItem(
    miniGoal: MiniGoal,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    showDate: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCompleted)
                    HabitGreen.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = GoalOrange
            )
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = miniGoal.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium
                ),
                color = if (isCompleted)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface
            )

            if (showDate && miniGoal.date != null) {
                Text(
                    text = "📅 ${formatMiniGoalDate(miniGoal.date)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else
                        GoalOrange,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

fun formatMiniGoalDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"))
    } catch (e: Exception) {
        dateString
    }
}