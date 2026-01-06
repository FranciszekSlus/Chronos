// Plik: ui/components/calendar/CalendarWeekView.kt - ZAKTUALIZOWANY
package com.tasker.chronos.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tasker.chronos.data.models.CustomEvent
import com.tasker.chronos.ui.components.AddCustomEventDialog
import com.tasker.chronos.ui.components.EditCustomEventDialog
import com.tasker.chronos.viewmodels.EventsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarWeekView(
    eventsViewModel: EventsViewModel,
    selectedDate: LocalDate
) {
    val customEvents by eventsViewModel.customEvents.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDialogDate by remember { mutableStateOf(selectedDate) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<CustomEvent?>(null) }


    val scrollState = rememberScrollState()

    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    //val miniGoalsForToday by (goalsViewModel?.getMiniGoalsForDate(selectedDate.toString())?: MutableStateFlow(emptyList())).collectAsState()

    LaunchedEffect(Unit) {
        val currentHour = java.time.LocalTime.now().hour
        val scrollToPosition = (currentHour * 120) - 200
        scrollState.scrollTo(scrollToPosition.coerceAtLeast(0))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Nagłówek z dniami tygodnia
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp)
            ) {
                Box(modifier = Modifier.width(60.dp))

                weekDays.forEach { day ->
                    WeekDayHeader(
                        date = day,
                        isToday = day == LocalDate.now(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Divider()

            // Siatka godzinowa z wydarzeniami
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                TimeLabelsColumn()

                weekDays.forEach { day ->
                    DayColumn(
                        date = day,
                        events = customEvents.filter { it.occursOnDate(day.toString()) },  // ✅ UŻYJ occursOnDate
                        onEventClick = { event ->
                            selectedEvent = event
                            showEditDialog = true
                        },
                        onEmptySlotClick = {
                            selectedDialogDate = day
                            showAddDialog = true
                        },
                        onEventResizeEnd = { event, newStartMinutes, newEndMinutes ->
                            val newStart = minutesToTime(newStartMinutes)
                            val newEnd = minutesToTime(newEndMinutes)
                            val updatedEvent = event.copy(
                                startTime = newStart,
                                endTime = newEnd
                            )
                            eventsViewModel.updateCustomEvent(updatedEvent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                selectedDialogDate = selectedDate
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj wydarzenie")
        }
    }

    if (showAddDialog) {
        AddCustomEventDialog(
            date = selectedDialogDate.toString(),
            onDismiss = { showAddDialog = false },
            onConfirm = { event ->
                eventsViewModel.addCustomEvent(event)
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && selectedEvent != null) {
        EditCustomEventDialog(
            event = selectedEvent!!,
            onDismiss = {
                showEditDialog = false
                selectedEvent = null
            },
            onSave = { updatedEvent ->
                eventsViewModel.updateCustomEvent(updatedEvent)
                showEditDialog = false
                selectedEvent = null
            },
            onDelete = { event ->
                eventsViewModel.deleteCustomEvent(event.id)
                showEditDialog = false
                selectedEvent = null
            }
        )
    }
}

@Composable
fun WeekDayHeader(
    date: LocalDate,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEE")),
            fontSize = 12.sp,
            color = if (isToday) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(50),
            color = if (isToday) MaterialTheme.colorScheme.primary
            else Color.Transparent,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 14.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) Color.White
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DayColumn(
    date: LocalDate,
    events: List<CustomEvent>,
    onEventClick: (CustomEvent) -> Unit,
    onEmptySlotClick: () -> Unit,
    onEventResizeEnd: (CustomEvent, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Tło z siatką godzinową
        Column(modifier = Modifier.fillMaxHeight()) {
            (0..23).forEach { _ ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        .clickable { onEmptySlotClick() }
                )
            }
        }

        // Wydarzenia
        events.forEach { event ->
            CompactResizableEvent(
                event = event,
                date = date,  // ✅ PRZEKAŻ DATĘ
                onClick = { onEventClick(event) },
                onResizeEnd = { newStart, newEnd ->
                    onEventResizeEnd(event, newStart, newEnd)
                }
            )
        }
    }
}

@Composable
fun BoxScope.CompactResizableEvent(
    event: CustomEvent,
    date: LocalDate,  // ✅ DODAJ PARAMETR
    onClick: () -> Unit,
    onResizeEnd: (Int, Int) -> Unit
) {
    var isSelected by remember { mutableStateOf(false) }

    // ✅ UŻYJ NOWYCH FUNKCJI - godziny zależne od dnia
    val startMinutes = parseTimeToMinutes(event.getStartTimeForDate(date.toString()))
    val endMinutes = parseTimeToMinutes(event.getEndTimeForDate(date.toString()))
    val durationMinutes = endMinutes - startMinutes

    val topOffset = (startMinutes * 2).dp
    val height = (durationMinutes * 2).dp.coerceAtLeast(30.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(height)
            .offset(y = topOffset)
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .align(Alignment.TopCenter)
            .zIndex(if (isSelected) 10f else 1f)
            .clickable {
                isSelected = !isSelected
                if (!isSelected) onClick()
            },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(event.color).copy(alpha = 0.85f)  // ✅ UŻYJ KOLORU Z EVENTU
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column {
                Text(
                    text = event.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = event.getStartTimeForDate(date.toString()),  // ✅ POKAŻ WŁAŚCIWĄ GODZINĘ
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}


