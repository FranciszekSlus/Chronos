// Plik: ui/components/calendar/DraggableDayView.kt
package com.tasker.chronos.ui.components.calendar

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tasker.chronos.data.models.CalendarEvent
import com.tasker.chronos.data.models.EventType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class DragState(
    val isDragging: Boolean = false,
    val draggedEvent: CalendarEvent? = null,
    val offset: Offset = Offset.Zero,
    val newTime: LocalTime? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableDayView(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onEventTimeChanged: (CalendarEvent, String) -> Unit, // Callback gdy zmieni się czas
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var dragState by remember { mutableStateOf(DragState()) }
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Generuj godziny (00:00 - 23:00)
    val hours = remember { (0..23).map { LocalTime.of(it, 0) } }

    Box(modifier = modifier.fillMaxSize()) {
        // Tło z liniami godzin
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )

            // Siatka godzinowa
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(hours) { hour ->
                    HourSlot(
                        hour = hour,
                        events = events.filter { event ->
                            event.startTime?.let {
                                try {
                                    LocalTime.parse(it).hour == hour.hour
                                } catch (e: Exception) {
                                    false
                                }
                            } ?: false
                        },
                        isDragging = dragState.isDragging,
                        isDropTarget = dragState.newTime?.hour == hour.hour,
                        onEventClick = onEventClick,
                        onEventDragStart = { event ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            dragState = DragState(
                                isDragging = true,
                                draggedEvent = event,
                                offset = Offset.Zero
                            )
                        },
                        onEventDrag = { event, offset ->
                            dragState = dragState.copy(offset = offset)

                            // Oblicz nową godzinę na podstawie przesunięcia
                            val hourHeight = with(density) { 80.dp.toPx() }
                            val draggedHours = (offset.y / hourHeight).roundToInt()
                            val originalTime = event.startTime?.let {
                                try {
                                    LocalTime.parse(it)
                                } catch (e: Exception) {
                                    LocalTime.of(12, 0)
                                }
                            } ?: LocalTime.of(12, 0)

                            val newTime = originalTime.plusHours(draggedHours.toLong())
                            dragState = dragState.copy(newTime = newTime)
                        },
                        onEventDragEnd = { event ->
                            if (dragState.newTime != null) {
                                val newTimeString = dragState.newTime!!.format(
                                    DateTimeFormatter.ofPattern("HH:mm")
                                )
                                onEventTimeChanged(event, newTimeString)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }

                            dragState = DragState()
                        }
                    )
                }
            }
        }

        // Przeciągany event (floating)
        if (dragState.isDragging && dragState.draggedEvent != null) {
            DraggingEventOverlay(
                event = dragState.draggedEvent!!,
                offset = dragState.offset
            )
        }
    }
}

@Composable
fun HourSlot(
    hour: LocalTime,
    events: List<CalendarEvent>,
    isDragging: Boolean,
    isDropTarget: Boolean,
    onEventClick: (CalendarEvent) -> Unit,
    onEventDragStart: (CalendarEvent) -> Unit,
    onEventDrag: (CalendarEvent, Offset) -> Unit,
    onEventDragEnd: (CalendarEvent) -> Unit
) {
    val dropTargetAlpha by animateFloatAsState(
        targetValue = if (isDropTarget) 1f else 0f,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // Linia godziny
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // Czas
            Text(
                text = hour.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .width(60.dp)
                    .padding(top = 4.dp, start = 8.dp)
            )

            // Linia
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        // Highlight dla drop target
        if (isDropTarget) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(dropTargetAlpha)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        )
                    )
            )
        }

        // Wydarzenia w tym slocie
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 70.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            events.forEach { event ->
                DraggableEventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    onDragStart = { onEventDragStart(event) },
                    onDrag = { offset -> onEventDrag(event, offset) },
                    onDragEnd = { onEventDragEnd(event) },
                    isDimmed = isDragging
                )
            }
        }
    }
}

@Composable
fun DraggableEventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    isDimmed: Boolean
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val alpha by animateFloatAsState(
        targetValue = when {
            isDragging -> 0.3f
            isDimmed -> 0.5f
            else -> 1f
        }
    )

    val backgroundColor = when (event.type) {
        EventType.HABIT -> Color(0xFF4CAF50)
        EventType.TASK -> Color(0xFF2196F3)
        EventType.GOAL -> Color(0xFFFF9800)
        EventType.CUSTOM -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .shadow(
                elevation = if (isDragging) 8.dp else 2.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        onDrag(dragOffset)
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffset = Offset.Zero
                        onDragEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = Offset.Zero
                        onDragEnd()
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, backgroundColor.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (event.startTime != null) {
                    Text(
                        text = event.startTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DraggingEventOverlay(
    event: CalendarEvent,
    offset: Offset
) {
    val backgroundColor = when (event.type) {
        EventType.HABIT -> Color(0xFF4CAF50)
        EventType.TASK -> Color(0xFF2196F3)
        EventType.GOAL -> Color(0xFFFF9800)
        EventType.CUSTOM -> Color(0xFF9E9E9E)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1000f)
    ) {
        Card(
            modifier = Modifier
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .width(200.dp)
                .shadow(16.dp, RoundedCornerShape(12.dp))
                .alpha(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(backgroundColor, RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = event.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Przeciągnij, by zmienić czas",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}