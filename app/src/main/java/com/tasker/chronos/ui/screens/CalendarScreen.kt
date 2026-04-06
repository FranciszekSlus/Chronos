// Plik: ui/screens/CalendarScreen.kt - ZINTEGROWANY Z INBOX
package com.tasker.chronos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasker.chronos.ui.components.calendar.CalendarDayView
import com.tasker.chronos.ui.components.calendar.MonthView
import com.tasker.chronos.ui.components.calendar.CalendarWeekView
import com.tasker.chronos.viewmodels.CalendarViewModel
import com.tasker.chronos.viewmodels.EventsViewModel
import com.tasker.chronos.viewmodels.GoalsViewModel
import com.tasker.chronos.viewmodels.HabitsViewModel
import com.tasker.chronos.viewmodels.TasksViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class CalendarViewType {
    MONTH, WEEK, DAY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel? = null,
    habitsViewModel: HabitsViewModel? = null,
    onNavigateToGoal: (String) -> Unit,
    onNavigateToSchedules: (String) -> Unit = {} // ✅ DODAJ TEN PARAMETR
) {
    val eventsViewModel: EventsViewModel = viewModel()
    val goalsViewModel: GoalsViewModel = viewModel()
    val tasksViewModel: TasksViewModel = viewModel() // ✅ DODANE
    val habitsViewModelLocal: HabitsViewModel = habitsViewModel ?: viewModel() // ✅ UŻYJ PRZEKAZANEGO LUB NOWEGO

    val selectedDate by eventsViewModel.selectedDate.collectAsState()
    val selectedMonth by eventsViewModel.selectedMonth.collectAsState()


    var currentView by remember { mutableStateOf(CalendarViewType.MONTH) }

    Scaffold(
        topBar = {
            CalendarTopBar(
                currentView = currentView,
                selectedDate = selectedDate,
                selectedMonth = selectedMonth,
                onViewChange = { currentView = it },
                onTodayClick = { eventsViewModel.goToToday() },
                onPreviousClick = {
                    when (currentView) {
                        CalendarViewType.MONTH -> eventsViewModel.previousMonth()
                        CalendarViewType.WEEK -> eventsViewModel.previousWeek()
                        CalendarViewType.DAY -> eventsViewModel.previousDay()
                    }
                },
                onNextClick = {
                    when (currentView) {
                        CalendarViewType.MONTH -> eventsViewModel.nextMonth()
                        CalendarViewType.WEEK -> eventsViewModel.nextWeek()
                        CalendarViewType.DAY -> eventsViewModel.nextDay()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentView) {
                CalendarViewType.MONTH -> {
                    MonthView(
                        selectedMonth = selectedMonth,
                        selectedDate = selectedDate,
                        eventTypesPerDay = emptyMap(),
                        onDateClick = { date ->
                            eventsViewModel.selectDate(date)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        eventsViewModel = eventsViewModel,
                        tasksViewModel = tasksViewModel,
                        goalsViewModel = goalsViewModel  // ✅ CZY TO JEST?
                    )
                }

                CalendarViewType.WEEK -> {
                    CalendarWeekView(
                        eventsViewModel = eventsViewModel,
                        selectedDate = selectedDate
                    )
                }

                CalendarViewType.DAY -> {
                    CalendarDayView(
                        eventsViewModel = eventsViewModel,
                        selectedDate = selectedDate,
                        tasksViewModel = tasksViewModel,
                        habitsViewModel = habitsViewModelLocal,
                        goalsViewModel = goalsViewModel,
                        onNavigateToGoal = onNavigateToGoal,
                        onNavigateToSchedules = { onNavigateToSchedules(selectedDate.toString()) }  // ✅ DODAJ
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    currentView: CalendarViewType,
    selectedDate: java.time.LocalDate,
    selectedMonth: YearMonth,
    onViewChange: (CalendarViewType) -> Unit,
    onTodayClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Nawigacja daty
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Poprzedni")
            }

            Text(
                text = when (currentView) {
                    CalendarViewType.MONTH -> {
                        selectedMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy"))
                    }
                    CalendarViewType.WEEK -> {
                        val weekStart = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
                        val weekEnd = weekStart.plusDays(6)
                        "${weekStart.format(DateTimeFormatter.ofPattern("d MMM"))} - ${weekEnd.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}"
                    }
                    CalendarViewType.DAY -> {
                        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                    }
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0x4D0064FF),  // rgba(0, 100, 255, 0.3)
                        offset = Offset(0f, 2f),
                        blurRadius = 15f
                    )
                )
            )

            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Następny")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Przyciski przełączania widoków + Dzisiaj
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Przełączanie widoków
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewTypeButton(
                    text = "Miesiąc",
                    isSelected = currentView == CalendarViewType.MONTH,
                    onClick = { onViewChange(CalendarViewType.MONTH) }
                )
                ViewTypeButton(
                    text = "Tydzień",
                    isSelected = currentView == CalendarViewType.WEEK,
                    onClick = { onViewChange(CalendarViewType.WEEK) }
                )
                ViewTypeButton(
                    text = "Dzień",
                    isSelected = currentView == CalendarViewType.DAY,
                    onClick = { onViewChange(CalendarViewType.DAY) }
                )
            }

            // Przycisk Dzisiaj
            OutlinedButton(
                onClick = onTodayClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Dzisiaj", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ViewTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}