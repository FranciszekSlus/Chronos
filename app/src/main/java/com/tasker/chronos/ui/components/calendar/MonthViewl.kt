// Plik: ui/components/calendar/CalendarMonthView.kt - ZAKTUALIZOWANY
package com.tasker.chronos.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.data.models.CustomEvent
import com.tasker.chronos.data.models.EventType
import com.tasker.chronos.data.models.Goal
import com.tasker.chronos.data.models.Task
import com.tasker.chronos.data.models.TaskPriority
import com.tasker.chronos.ui.components.AddCustomEventDialog
import com.tasker.chronos.viewmodels.EventsViewModel
import com.tasker.chronos.viewmodels.GoalsViewModel
import com.tasker.chronos.viewmodels.TasksViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthView(
    selectedMonth: YearMonth,
    selectedDate: LocalDate,
    eventTypesPerDay: Map<String, List<EventType>>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    eventsViewModel: EventsViewModel? = null,
    tasksViewModel: TasksViewModel? = null,  // ✅ DODAJ
    goalsViewModel: GoalsViewModel? = null   // ✅ DODAJ
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDialogDate by remember { mutableStateOf(LocalDate.now()) }

    // Custom Wydarzenia
    // ✅ Custom Wydarzenia (BEZ nawyków z inbox)
    // ✅ Custom Wydarzenia (BEZ nawyków - filtruj po sourceHabitId)
    val customEvents = if (eventsViewModel != null) {
        eventsViewModel.customEvents.collectAsState().value
            .filter { it.sourceHabitId == null }  // ✅ Ukryj wszystkie wydarzenia pochodzące z nawyków
    } else {
        emptyList()
    }


    val highPriorityTasks = if (tasksViewModel != null) {
        val allTasks = tasksViewModel.allTasks.collectAsState().value
        allTasks.filter { it.priority == TaskPriority.HIGH && it.date != null }
    } else {
        emptyList<Task>()
    }

    val goalsWithEndDate = if (goalsViewModel != null) {
        val allGoals = goalsViewModel.allGoals.collectAsState().value
        allGoals.filter { it.endDate != null }
    } else {
        emptyList<Goal>()
    }

    android.util.Log.d("MonthView", "🎯 High priority tasks: ${highPriorityTasks.size}")
    android.util.Log.d("MonthView", "🏆 Goals with end date: ${goalsWithEndDate.size}")

    // Mapa kolorów dla custom eventów
    val eventColorsPerDay = remember(customEvents) {
        val colorsMap = mutableMapOf<String, MutableList<Long>>()
        customEvents.forEach { event ->
            if (event.showInMonthView) {  // ✅ DODAJ TEN WARUNEK
                val allDates = event.getAllDates()
                allDates.forEach { date ->
                    colorsMap.getOrPut(date) { mutableListOf() }.add(event.color)
                }
            }
        }
        colorsMap
    }

    // ✅ NOWA MAPA: Zadania wysokiego priorytetu per dzień
    // ✅ NOWA MAPA: Zadania wysokiego priorytetu per dzień (z dodatkową walidacją)
    val highPriorityTasksPerDay = remember(highPriorityTasks) {
        highPriorityTasks
            .filter { it.date != null && it.priority == TaskPriority.HIGH }  // ✅ PODWÓJNA WALIDACJA
            .groupBy { it.date!! }
    }
    // ✅ NOWA MAPA: Cele z datą końcową per dzień
    val goalsPerDay = remember(goalsWithEndDate) {
        goalsWithEndDate
            .filter { it.endDate != null }  // ✅ Już filtrowane, ale dla pewności
            .groupBy { it.endDate!! }       // ✅ Wymuś non-null
    }

    val firstDayOfMonth = selectedMonth.atDay(1)
    val lastDayOfMonth = selectedMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val daysInMonth = selectedMonth.lengthOfMonth()

    val previousMonth = selectedMonth.minusMonths(1)
    val daysFromPrevMonth = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
    val totalCells = 42

    val allDays = mutableListOf<LocalDate>()

    for (i in daysFromPrevMonth - 1 downTo 0) {
        allDays.add(firstDayOfMonth.minusDays(i.toLong() + 1))
    }

    for (day in 1..daysInMonth) {
        allDays.add(selectedMonth.atDay(day))
    }

    val remainingCells = totalCells - allDays.size
    for (i in 1..remainingCells) {
        allDays.add(lastDayOfMonth.plusDays(i.toLong()))
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Nagłówek z dniami tygodnia
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Siatka dni
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allDays.size) { index ->
                    val date = allDays[index]
                    val dateString = date.toString()
                    val isCurrentMonth = date.month == selectedMonth.month
                    val isToday = date == LocalDate.now()
                    val isSelected = date == selectedDate

                    // ✅ NOWE: Zbierz kolory z różnych źródeł
                    // ✅ Zbierz kolory z różnych źródeł
                    val colors = mutableListOf<Long>()

// 1. Custom wydarzenia
                    eventColorsPerDay[dateString]?.let { colors.addAll(it) }

// 2. Zadania WYSOKIEGO priorytetu (czerwony) - POPRAWIONE FILTROWANIE
                    highPriorityTasksPerDay[dateString]?.let { tasksOnThisDay ->
                        // ✅ DODATKOWA WALIDACJA - tylko HIGH priority
                        if (tasksOnThisDay.any { it.priority == TaskPriority.HIGH }) {
                            colors.add(0xFFF44336L)  // Czerwony tylko dla HIGH
                        }
                    }

                    // 3. Cele z datą końcową (złoty gradient marker)
                    val hasGoalDeadline = goalsPerDay[dateString] != null

                    DayCell(
                        date = date,
                        isCurrentMonth = isCurrentMonth,
                        isToday = isToday,
                        isSelected = isSelected,
                        eventColors = colors,
                        hasGoalDeadline = hasGoalDeadline,  // ✅ NOWY PARAMETR
                        onClick = {
                            onDateClick(date)
                        },
                        onLongClick = {
                            if (eventsViewModel != null) {
                                selectedDialogDate = date
                                showAddDialog = true
                            }
                        }
                    )
                }
            }
        }

        if (eventsViewModel != null) {
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
    }

    if (showAddDialog && eventsViewModel != null) {
        AddCustomEventDialog(
            date = selectedDialogDate.toString(),
            showMonthViewToggle = false,
            onDismiss = { showAddDialog = false },
            onConfirm = { event ->
                eventsViewModel.addCustomEvent(event)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    eventColors: List<Long>,
    hasGoalDeadline: Boolean,  // ✅ NOWY PARAMETR
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // ✅ NOWE: Jeśli cel ma deadline, pokaż dekoracyjną ikonę
            if (hasGoalDeadline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),  // Złoty
                                    Color(0xFFFFA500)   // Pomarańczowy
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Cel deadline",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (hasGoalDeadline) 2.dp else 4.dp))

            // Numer dnia
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Kropki dla wydarzeń (custom + zadania wysokiego priorytetu)
            if (eventColors.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    eventColors.take(3).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .padding(horizontal = 1.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                        )
                    }
                }
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
    } catch (e: Exception) {
        dateString
    }
}