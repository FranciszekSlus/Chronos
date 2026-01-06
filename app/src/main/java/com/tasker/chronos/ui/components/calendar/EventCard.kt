// Plik: ui/components/calendar/EventCard.kt
package com.tasker.chronos.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.CalendarEvent
import com.tasker.chronos.data.models.EventType

@Composable
fun EventCard(
    event: CalendarEvent,
    onClick: () -> Unit
) {
    val backgroundColor = when (event.type) {
        EventType.HABIT -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        EventType.TASK -> Color(0xFF2196F3).copy(alpha = 0.15f)
        EventType.GOAL -> Color(0xFFFF9800).copy(alpha = 0.15f)
        EventType.CUSTOM -> Color(0xFF9E9E9E).copy(alpha = 0.15f)
    }

    val borderColor = when (event.type) {
        EventType.HABIT -> Color(0xFF4CAF50)
        EventType.TASK -> Color(0xFF2196F3)
        EventType.GOAL -> Color(0xFFFF9800)
        EventType.CUSTOM -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pasek koloru z lewej strony
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(borderColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Tytuł wydarzenia
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (event.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.alpha(if (event.isCompleted) 0.6f else 1f)
                )

                // Godzina (jeśli jest)
                if (event.startTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(event.startTime)
                            if (event.endTime != null) {
                                append(" - ${event.endTime}")
                            }
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Typ wydarzenia
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (event.type) {
                        EventType.HABIT -> "Nawyk"
                        EventType.TASK -> "Zadanie"
                        EventType.GOAL -> "Cel"
                        EventType.CUSTOM -> "Wydarzenie"
                    },
                    fontSize = 12.sp,
                    color = borderColor,
                    fontWeight = FontWeight.Medium
                )
            }

            // Status wykonania
            if (event.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Wykonane",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}