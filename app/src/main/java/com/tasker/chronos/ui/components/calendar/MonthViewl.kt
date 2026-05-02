// Plik: ui/components/calendar/CalendarMonthView.kt - ZAKTUALIZOWANY
package com.tasker.chronos.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlin.math.roundToInt

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
    var isMonthExpanded by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val monthCellExpandProgress by animateFloatAsState(
        targetValue = if (isMonthExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "month_cell_expand_animation"
    )

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
    val eventTitlesPerDay = remember(customEvents, highPriorityTasks, goalsWithEndDate) {
        val titlesMap = mutableMapOf<String, MutableList<String>>()

        customEvents
            .filter { it.showInMonthView }
            .forEach { event ->
                event.getAllDates().forEach { date ->
                    titlesMap.getOrPut(date) { mutableListOf() }.add(event.title)
                }
            }

        highPriorityTasks
            .forEach { task ->
                task.date?.let { date ->
                    titlesMap.getOrPut(date) { mutableListOf() }.add(task.title)
                }
            }

        goalsWithEndDate
            .forEach { goal ->
                goal.endDate?.let { date ->
                    titlesMap.getOrPut(date) { mutableListOf() }.add("Cel: ${goal.title}")
                }
            }

        titlesMap.mapValues { (_, titles) -> titles.distinct() }
    }

    val firstDayOfMonth = selectedMonth.atDay(1)
    val lastDayOfMonth = selectedMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val daysInMonth = selectedMonth.lengthOfMonth()

    val previousMonth = selectedMonth.minusMonths(1)
    // POPRAWNY KOD:
    val daysFromPrevMonth = if (firstDayOfWeek == 7) 6 else firstDayOfWeek - 1
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

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(isMonthExpanded) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulator += dragAmount
                                val swipeThreshold = 80f

                                if (dragAccumulator >= swipeThreshold && !isMonthExpanded) {
                                    isMonthExpanded = true
                                    dragAccumulator = 0f
                                } else if (dragAccumulator <= -swipeThreshold && isMonthExpanded) {
                                    isMonthExpanded = false
                                    dragAccumulator = 0f
                                }
                            },
                            onDragEnd = { dragAccumulator = 0f },
                            onDragCancel = { dragAccumulator = 0f }
                        )
                    }
            ) {
                val gridHeight = maxHeight
                val rowSpacing = 4.dp
                val expandedCellHeight = ((gridHeight - (rowSpacing * 5)) / 6).coerceAtLeast(56.dp)
                val collapsedCellHeight = (expandedCellHeight * 0.68f).coerceAtLeast(56.dp)
                val animatedCellHeight = lerp(
                    start = collapsedCellHeight,
                    stop = expandedCellHeight,
                    fraction = monthCellExpandProgress
                )

                // Siatka dni - zawsze 6 rzędów widoczne w obu stanach.
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxSize(),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(rowSpacing)
                ) {
                    items(allDays.size) { index ->
                        val date = allDays[index]
                        val dateString = date.toString()
                        val isCurrentMonth = date.month == selectedMonth.month
                        val isToday = date == LocalDate.now()
                        val isSelected = date == selectedDate

                        val colors = mutableListOf<Long>()
                        eventColorsPerDay[dateString]?.let { colors.addAll(it) }
                        highPriorityTasksPerDay[dateString]?.let { tasksOnThisDay ->
                            if (tasksOnThisDay.any { it.priority == TaskPriority.HIGH }) {
                                colors.add(0xFFF44336L)
                            }
                        }
                        val hasGoalDeadline = goalsPerDay[dateString] != null

                        DayCell(
                            date = date,
                            isCurrentMonth = isCurrentMonth,
                            isToday = isToday,
                            isSelected = isSelected,
                            eventColors = colors,
                            eventTitles = eventTitlesPerDay[dateString].orEmpty(),
                            expandProgress = monthCellExpandProgress,
                            cellHeight = animatedCellHeight,
                            hasGoalDeadline = hasGoalDeadline,
                            onClick = { onDateClick(date) },
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
    eventTitles: List<String>,
    expandProgress: Float,
    cellHeight: androidx.compose.ui.unit.Dp,
    hasGoalDeadline: Boolean,  // ✅ NOWY PARAMETR
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shouldShowTitles = expandProgress > 0.2f
    val maxTitles = (1 + (expandProgress * 3f)).roundToInt().coerceIn(1, 4)
    val dotsSpreadProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "month_dots_spread"
    )

    Box(
        modifier = Modifier
            .height(cellHeight)
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
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .height(8.dp)
                        .width(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val collapsedStep = 4f
                    val expandedStep = 9f
                    val stepPx = collapsedStep + (expandedStep - collapsedStep) * dotsSpreadProgress
                    val count = eventColors.take(3).size
                    eventColors.take(3).forEachIndexed { index, color ->
                        val x = ((index - (count - 1) / 2f) * stepPx).toInt()
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(x = x, y = 0) }
                                .zIndex(index.toFloat())
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                        )
                    }
                }
            }

            if (shouldShowTitles && eventTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    eventTitles.take(maxTitles).forEach { title ->
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isCurrentMonth) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                            }
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