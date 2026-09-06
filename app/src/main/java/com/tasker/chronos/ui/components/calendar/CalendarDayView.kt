    // Plik: ui/components/calendar/CalendarDayView.kt - KOMPLETNY
    package com.tasker.chronos.ui.components.calendar
    
    import android.content.Intent
    import androidx.compose.animation.AnimatedVisibility
    import androidx.compose.animation.slideInHorizontally
    import androidx.compose.animation.slideOutHorizontally
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.gestures.awaitFirstDown
    import androidx.compose.foundation.gestures.detectDragGestures
    import androidx.compose.foundation.gestures.detectTapGestures
    import androidx.compose.foundation.gestures.drag
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalDensity
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.draw.alpha
    import androidx.compose.ui.geometry.Offset
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.Shadow
    import androidx.compose.ui.graphics.graphicsLayer
    import androidx.compose.ui.input.pointer.pointerInput
    import androidx.compose.ui.layout.onGloballyPositioned
    import androidx.compose.ui.layout.positionInWindow
    import androidx.compose.ui.layout.positionOnScreen
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.IntOffset
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.compose.ui.zIndex
    import androidx.core.text.color
    import androidx.xr.compose.testing.toDp
    import com.tasker.chronos.MainActivity
    import com.tasker.chronos.data.models.CustomEvent
    import com.tasker.chronos.data.models.Goal
    import com.tasker.chronos.data.models.Habit
    import com.tasker.chronos.data.models.MiniGoal
    import com.tasker.chronos.data.models.Task
    import com.tasker.chronos.data.models.TaskPriority
    import com.tasker.chronos.data.models.toColor
    import com.tasker.chronos.notifications.TaskReminderScheduler
    import com.tasker.chronos.ui.components.AddCustomEventDialog
    import com.tasker.chronos.ui.components.EditCustomEventDialog
    import com.tasker.chronos.ui.components.EditTaskSheet
    import com.tasker.chronos.ui.theme.GoalGold
    import com.tasker.chronos.ui.theme.HabitGreen
    import com.tasker.chronos.ui.theme.chronosAccentGradient
    import com.tasker.chronos.ui.theme.textWithBlueGlow
    import com.tasker.chronos.viewmodels.EventsViewModel
    import com.tasker.chronos.viewmodels.GoalsViewModel
    import com.tasker.chronos.viewmodels.HabitsViewModel
    import com.tasker.chronos.viewmodels.TasksViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import java.time.LocalDate
    import java.time.LocalTime
    import java.time.format.DateTimeFormatter
    import kotlin.math.roundToInt

    /**
     * Zadanie z datą i godziną na siatce dnia — gdy nie ma już CustomEvent powiązanego (sourceTaskId).
     */
    private fun taskToGridCustomEvent(task: Task): CustomEvent {
        val start = task.time ?: "09:00"
        val end = try {
            LocalTime.parse(start).plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            start
        }
        return CustomEvent(
            id = "task_${task.id}",
            title = task.title,
            description = task.description,
            date = task.date ?: "",
            startTime = start,
            endTime = end,
            color = when (task.priority) {
                TaskPriority.LOW -> 0xFF4CAF50L
                TaskPriority.MEDIUM -> 0xFFFFC107L
                TaskPriority.HIGH -> 0xFFF44336L
            },
            hasReminder = task.hasReminder,
            reminderMinutesBefore = task.reminderMinutesBefore,
            sourceTaskId = task.id
        )
    }

    @Composable
    fun CalendarDayView(
        eventsViewModel: EventsViewModel,
        selectedDate: LocalDate,
        tasksViewModel: TasksViewModel,
        habitsViewModel: HabitsViewModel? = null,
        goalsViewModel: GoalsViewModel? = null,
        onNavigateToGoal: (String) -> Unit = {},
        onGoalDialogDismissed: () -> Unit = {},
        onNavigateToSchedules: () -> Unit = {},
        deepLinkOpenEventId: String? = null,
        deepLinkNonce: Long? = null,
        onDeepLinkOpenConsumed: () -> Unit = {}

    ) {

        val context = LocalContext.current

        // ✅ Inbox: zadania bez daty LUB zadania z datą ale BEZ godziny (tylko dla wybranego dnia)
        val dateString = selectedDate.toString()
        val density = LocalDensity.current
        val allCustomEvents by eventsViewModel.customEvents.collectAsState()

        val allTasksForDay = tasksViewModel.allTasks.collectAsState().value
            .filter { it.date == dateString && !it.isCompleted }

        val customEvents = remember(allCustomEvents, selectedDate) {
            allCustomEvents.filter { event ->
                event.occursOnDate(selectedDate.toString())
            }
        }

        val gridEvents = remember(customEvents, allTasksForDay) {
            val linkedTaskIds = customEvents.mapNotNull { it.sourceTaskId }.toSet()
            val fromTasksOnly = allTasksForDay
                .filter { it.time != null && it.id !in linkedTaskIds }
                .map { taskToGridCustomEvent(it) }
            customEvents + fromTasksOnly
        }

// ✅ 4. Inbox: zadania bez daty LUB z datą ale BEZ godziny
        val inboxTasks by tasksViewModel.inboxTasks.collectAsState()

        val inboxTasksForDay = remember(allTasksForDay, inboxTasks) {
            val tasksForDayWithoutTime = allTasksForDay.filter { it.time == null }
            inboxTasks + tasksForDayWithoutTime
        }

        val allHabits by (habitsViewModel?.habits ?: MutableStateFlow(emptyList())).collectAsState()
        // ✅ Pobierz mini-cele dla wybranego dnia
        // ✅ POPRAWKA: Usuń goalsViewModel z remember - tylko selectedDate
        val miniGoalsForToday by remember(selectedDate) {  // ❌ USUŃ goalsViewModel
            android.util.Log.d("CalendarView", "🔄 Tworzę flow dla daty: $selectedDate")
            goalsViewModel?.getMiniGoalsForDate(selectedDate.toString())
                ?: MutableStateFlow(emptyList())
        }.collectAsState()

        LaunchedEffect(miniGoalsForToday, selectedDate) {
            android.util.Log.d("CalendarView", "========================================")
            android.util.Log.d("CalendarView", "📅 Data: $selectedDate")
            android.util.Log.d("CalendarView", "🎯 Mini-cele w inbox: ${miniGoalsForToday.size}")
            miniGoalsForToday.forEach { (goal, miniGoal) ->
                android.util.Log.d("CalendarView", "  ✓ '${miniGoal.title}' z: '${goal.title}', data: ${miniGoal.date}")
            }
            android.util.Log.d("CalendarView", "========================================")
        }
    
        var showAddDialog by remember { mutableStateOf(false) }
        var showEditDialog by remember { mutableStateOf(false) }
        var selectedTask by remember { mutableStateOf<Task?>(null) }
        var showEditTaskSheet by remember { mutableStateOf(false) }
        var selectedEvent by remember { mutableStateOf<CustomEvent?>(null) }
        // Powiadomienie o wydarzeniu: tylko widok dnia/kalendarza, bez auto-edycji
        LaunchedEffect(deepLinkNonce, deepLinkOpenEventId) {
            if (deepLinkNonce == null) return@LaunchedEffect
            onDeepLinkOpenConsumed()
        }
        val scrollState = rememberScrollState()
        var showInboxDrawer by remember { mutableStateOf(false) }
        var draggedTask by remember { mutableStateOf<Task?>(null) }
        var draggedHabit by remember { mutableStateOf<Habit?>(null) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }
        var draggedMiniGoal by remember { mutableStateOf<Pair<Goal, MiniGoal>?>(null) }  // ✅ DODAJ
        var gridTopOffset by remember { mutableStateOf(0f) }


    
        LaunchedEffect(Unit) {
            val currentHour = LocalTime.now().hour
            val scrollToPosition = (currentHour * 120) - 200
            scrollState.scrollTo(scrollToPosition.coerceAtLeast(0))
        }


    
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDate.format(
                                java.time.format.DateTimeFormatter.ofPattern(
                                    "EEEE, d MMMM"
                                )
                            ),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            style = textWithBlueGlow()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                onClick = { onNavigateToSchedules() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewDay,
                                    contentDescription = "Schematy dnia",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Schematy",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // ✅ NOWY KOD (wklej):
                        BadgedBox(
                            badge = {
                                // ✅ POPRAWKA: WSZYSTKIE nawyki (codzienne + tygodniowe + miesięczne)
                                val allHabitsCount = allHabits.size  // ✅ Wszystkie nawyki w inbox

                                // Mini-cele bez daty (codzienne)
                                val dailyMiniGoalsCount = miniGoalsForToday.count { (_, miniGoal) ->
                                    miniGoal.date == null
                                }

                                // ✅ BASELINE = wszystkie nawyki + mini-cele bez daty
                                val baselineCount = allHabitsCount + dailyMiniGoalsCount

                                // Wszystkie elementy w inbox
                                val totalCount =
                                    inboxTasksForDay.size + allHabits.size + miniGoalsForToday.size

                                // ✅ Niebieski TYLKO gdy baseline (czerwony gdy więcej)
                                val isBaseline = (totalCount == baselineCount)

                                android.util.Log.d(
                                    "InboxBadge",
                                    "📊 baseline=$baselineCount (nawyki wszystkie:$allHabitsCount, mini bez daty:$dailyMiniGoalsCount)"
                                )
                                android.util.Log.d("InboxBadge", "📊 total=$totalCount")
                                android.util.Log.d(
                                    "InboxBadge",
                                    "📊 isBaseline=$isBaseline (${if (isBaseline) "NIEBIESKI" else "CZERWONY"})"
                                )

                                if (totalCount > 0) {
                                    Badge(
                                        containerColor = if (isBaseline)
                                            MaterialTheme.colorScheme.primary      // ✅ NIEBIESKI - tylko baseline
                                        else
                                            MaterialTheme.colorScheme.error        // ✅ CZERWONY - są dodatkowe
                                    ) {
                                        Text(
                                            text = totalCount.toString(),
                                            fontSize = 10.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { showInboxDrawer = !showInboxDrawer }) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = "Zadania i Nawyki",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
    
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        TimeLabelsColumn()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(Unit) {  // ✅ DODAJ TO
                                    detectTapGestures {
                                        if (showInboxDrawer) {
                                            showInboxDrawer = false
                                            android.util.Log.d(
                                                "CalendarView",
                                                "Closed drawer by tapping grid"
                                            )
                                        }
                                    }
                                }
                                .onGloballyPositioned { coordinates ->
                                    gridTopOffset = coordinates.positionInWindow().y
                                    android.util.Log.d(
                                        "GridPosition",
                                        "Grid top at: $gridTopOffset"
                                    )
                                }
                        ) {
                            HourGridLines()
    
                            EventsLayer(
                                events = gridEvents,
                                selectedDate = selectedDate,
                                onEventClick = { event ->
                                    selectedEvent = event
                                    showEditDialog = true
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
                                draggedTask = draggedTask,
                                draggedHabit = draggedHabit,
                                onDropTask = { task, hour ->
                                    val startTime = String.format("%02d:00", hour)
                                    val endTime = String.format("%02d:00", (hour + 1) % 24)

                                    eventsViewModel.addCustomEvent(
                                        CustomEvent(
                                            title = task.title,
                                            description = task.description,
                                            date = selectedDate.toString(),
                                            startTime = startTime,
                                            endTime = endTime,
                                            color = when (task.priority) {
                                                TaskPriority.LOW -> 0xFF4CAF50L
                                                TaskPriority.MEDIUM -> 0xFFFFC107L
                                                TaskPriority.HIGH -> 0xFFF44336L
                                            },
                                            hasReminder = task.hasReminder,
                                            reminderMinutesBefore = task.reminderMinutesBefore,
                                            sourceTaskId = task.id
                                        )
                                    )
                                    tasksViewModel.assignDateToTask(task.id, selectedDate.toString(), startTime)

                                    draggedTask = null
                                },
                                onDropHabit = { habit, hour ->
                                    val startTime = String.format("%02d:00", hour)
                                    val endTime = String.format("%02d:00", (hour + 1) % 24)

                                    eventsViewModel.addCustomEvent(
                                        CustomEvent(
                                            title = draggedTask!!.title,
                                            description = draggedTask!!.description,
                                            date = selectedDate.toString(),
                                            startTime = startTime,
                                            endTime = endTime,
                                            color = when (draggedTask!!.priority) {
                                                TaskPriority.LOW -> 0xFF4CAF50L
                                                TaskPriority.MEDIUM -> 0xFFFFC107L
                                                TaskPriority.HIGH -> 0xFFF44336L
                                            },
                                            hasReminder = draggedTask!!.hasReminder,
                                            reminderMinutesBefore = draggedTask!!.reminderMinutesBefore,
                                            sourceTaskId = draggedTask!!.id  // ✅ DODAJ TO
                                        )
                                    )
                                    draggedHabit = null
                                }
                            )
                            // ✅ NOWE: Deadline celów na końcu dnia
                            val goalsWithDeadline = remember(goalsViewModel, selectedDate) {
                                goalsViewModel?.allGoals?.value?.filter { goal ->
                                    goal.endDate == selectedDate.toString()
                                } ?: emptyList()
                            }

                            if (goalsWithDeadline.isNotEmpty()) {
                                goalsWithDeadline.forEach { goal ->
                                    GoalDeadlineBlock(
                                        goal = goal,
                                        onClick = {
                                            onNavigateToGoal(goal.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
    
                AnimatedVisibility(
                    visible = showInboxDrawer,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it })
                ) {
                    InboxDrawer(
                        tasks = inboxTasksForDay,  // ✅ Użyj nowej zmiennej
                        habits = allHabits,
                        miniGoals = miniGoalsForToday,
                        onTaskDragStart = { task, position ->
                            draggedTask = task
                            dragOffset = position
                            android.util.Log.d("CalendarView", "Drag started at: $position")
                        },
                        onMiniGoalDragStart = { goal, miniGoal, position ->  // ✅ NOWE
                            draggedMiniGoal = Pair(goal, miniGoal)
                            dragOffset = position
                        },
                        onMiniGoalDrag = { newPosition ->
                            dragOffset = newPosition
                        },
                        // ✅ NOWY KOD (prawidłowa struktura):
                        // ✅ POPRAWIONY onMiniGoalDragEnd
                        onMiniGoalDragEnd = {
                            android.util.Log.d("CalendarView", "🎯 onMiniGoalDragEnd wywołane");    if (draggedMiniGoal != null) {
                            val (goal, miniGoal) = draggedMiniGoal!!
                            val fingerY = dragOffset.y
                            val fingerX = dragOffset.x

                            val isOverCalendar = fingerX < 1000f && fingerY > 100f

                            if (!isOverCalendar) {
                                android.util.Log.d("CalendarView", "MiniGoal dropped outside - returning to inbox")
                                draggedMiniGoal = null
                            } else {
                                // Logika obliczania pozycji musi być TUTAJ (wewnątrz else od isOverCalendar)
                                val headerHeight = 490f
                                val cardOffset = with(density) { 120.dp.toPx() }

                                val positionInGrid = (fingerY - headerHeight + cardOffset) + scrollState.value
                                val dp = with(density) { positionInGrid.toDp().value }
                                val totalMinutes = (dp / 2).toInt().coerceIn(0, 1439)

                                val hours = totalMinutes / 60
                                val minutes = totalMinutes % 60
                                val roundedMinutes = (minutes / 15) * 15

                                val startTime = String.format("%02d:%02d", hours, roundedMinutes)
                                val endTime = String.format("%02d:%02d", (hours + 1) % 24, roundedMinutes)

                                // ✅ Twórz CustomEvent na podstawie MINI-CELU (nie taska!)
                                val colorLong = try {
                                    android.graphics.Color.parseColor(goal.color.toString()).toLong()
                                } catch (e: Exception) {
                                    0xFF2196F3L
                                }

                                eventsViewModel.addCustomEvent(
                                    CustomEvent(
                                        title = miniGoal.title,
                                        description = "Z celu: ${goal.title}",
                                        date = selectedDate.toString(),
                                        startTime = startTime,
                                        endTime = endTime,
                                        color = colorLong,
                                        sourceGoalId = goal.id,
                                        sourceMiniGoalId = miniGoal.id
                                    )
                                )

                                // Usuń datę z mini-celu żeby zniknął z listy do przeciągania
                                goalsViewModel?.updateMiniGoal(
                                    goal.id,
                                    miniGoal.copy(date = null)
                                )

                                android.util.Log.d("CalendarView", "✅ Mini-cel '${miniGoal.title}' zaplanowany na $startTime")
                                draggedMiniGoal = null
                            }
                        }
                        },
                        onTaskDrag = { newPosition ->
                            dragOffset = newPosition // ✅ Aktualizuj podczas przeciągania
                            android.util.Log.d("CalendarView", "Dragging to: $newPosition")
                        },
                        onHabitDragStart = { habit, position ->
                            draggedHabit = habit
                            dragOffset = position
                            android.util.Log.d("CalendarView", "Habit drag started at: $position")
                        },
                        onHabitDrag = { newPosition ->
                            dragOffset = newPosition
                            android.util.Log.d("CalendarView", "Dragging habit to: $newPosition")
                        },
                        onTaskDragEnd = {
                            if (draggedTask != null) {
                                val fingerY = dragOffset.y
                                val fingerX = dragOffset.x

                                val isOverCalendar = fingerX < 1000f && fingerY > 100f

                                if (!isOverCalendar) {
                                    android.util.Log.d("CalendarView", "Task dropped outside - returning to inbox")
                                    draggedTask = null
                                } else {
                                    val headerHeight = 490f
                                    val cardOffset = with(density) { 120.dp.toPx() }

                                    val positionInGrid = (fingerY - headerHeight + cardOffset) + scrollState.value
                                    val dp = with(density) { positionInGrid.toDp().value }
                                    val totalMinutes = (dp / 2).toInt().coerceIn(0, 1439)

                                    val hours = totalMinutes / 60
                                    val minutes = totalMinutes % 60
                                    val roundedMinutes = (minutes / 15) * 15

                                    val startTime = String.format("%02d:%02d", hours, roundedMinutes)
                                    val endTime = String.format("%02d:%02d", (hours + 1) % 24, roundedMinutes)

                                    val taskToConvert = draggedTask!!
                                    eventsViewModel.addCustomEvent(
                                        CustomEvent(
                                            title = taskToConvert.title,
                                            description = taskToConvert.description,
                                            date = selectedDate.toString(),
                                            startTime = startTime,
                                            endTime = endTime,
                                            color = when (taskToConvert.priority) {
                                                TaskPriority.LOW -> 0xFF4CAF50L
                                                TaskPriority.MEDIUM -> 0xFFFFC107L
                                                TaskPriority.HIGH -> 0xFFF44336L
                                            },
                                            hasReminder = taskToConvert.hasReminder,
                                            reminderMinutesBefore = taskToConvert.reminderMinutesBefore,
                                            sourceTaskId = taskToConvert.id
                                        )
                                    )
                                    tasksViewModel.assignDateToTask(taskToConvert.id, selectedDate.toString(), startTime)

                                    android.util.Log.d("CalendarView", "✅ Zadanie '${taskToConvert.title}' zaplanowane na siatce; Task pozostaje na liście: $startTime")
                                    draggedTask = null
                                }
                            }
                        },
                        onHabitDragEnd = {
                            if (draggedHabit != null) {
                                val fingerY = dragOffset.y
                                val fingerX = dragOffset.x

                                val isOverCalendar = fingerX < 1000f && fingerY > 100f

                                if (!isOverCalendar) {
                                    android.util.Log.d("CalendarView", "Habit dropped outside - returning to inbox")
                                    draggedHabit = null
                                } else {
                                    val headerHeight = 490f
                                    val cardOffset = with(density) { 120.dp.toPx() }

                                    val positionInGrid = (fingerY - headerHeight + cardOffset) + scrollState.value
                                    val dp = with(density) { positionInGrid.toDp().value }
                                    val totalMinutes = (dp / 2).toInt().coerceIn(0, 1439)

                                    android.util.Log.d("CalendarView", "fingerY=$fingerY, cardOffset=$cardOffset, posInGrid=$positionInGrid, dp=$dp, minutes=$totalMinutes")

                                    val hours = totalMinutes / 60
                                    val minutes = totalMinutes % 60
                                    val roundedMinutes = (minutes / 15) * 15

                                    val startTime = String.format("%02d:%02d", hours, roundedMinutes)
                                    val endTime = String.format("%02d:%02d", (hours + 1) % 24, roundedMinutes)

                                    eventsViewModel.addCustomEvent(
                                        CustomEvent(
                                            title = draggedHabit!!.name,
                                            description = "Z nawyku",
                                            date = selectedDate.toString(),
                                            startTime = startTime,
                                            endTime = endTime,
                                            color = 0xFF4CAF50L,
                                            hasReminder = draggedHabit!!.hasReminder,
                                            reminderMinutesBefore = 15,
                                            sourceHabitId = draggedHabit!!.id
                                        )
                                    )

                                    draggedHabit = null
                                }
                            }
                        },
                        onClose = { showInboxDrawer = false }

                    )
                }

            }


            // ZNAJDŹ TEN BLOK I ZASTĄP GO:
            if (draggedTask != null || draggedHabit != null || draggedMiniGoal != null) {
                var currentOffset by remember(draggedTask, draggedHabit, draggedMiniGoal) {
                    mutableStateOf(dragOffset)
                }

                LaunchedEffect(dragOffset) {
                    currentOffset = dragOffset
                }

                Card(
                    modifier = Modifier
                        .zIndex(999f)
                        .offset {
                            IntOffset(
                                (currentOffset.x - 90).roundToInt(),
                                (currentOffset.y - 30).roundToInt()
                            )
                        }
                        .width(180.dp),
                    colors = CardDefaults.cardColors(  // ✅ POPRAWKA: użyj colors zamiast containerColor
                        containerColor = when {
                            draggedTask != null -> draggedTask!!.priority.toColor().copy(alpha = 0.9f)
                            draggedMiniGoal != null -> GoalGold.copy(alpha = 0.9f)
                            else -> HabitGreen.copy(alpha = 0.9f)
                        }
                    )
                )  {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = draggedTask?.title ?: draggedHabit?.name ?: draggedMiniGoal?.second?.title ?: "",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }


            // ✅ FAB (bez zmian)
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj wydarzenie")
            }
        }

        if (showAddDialog) {
            // ✅ Znajdź ostatnie wydarzenie dzisiaj (najwyższy endTime)
            val lastEvent = customEvents
                .filter { it.date == selectedDate.toString() }
                .maxByOrNull { parseTimeToMinutes(it.endTime) }

            AddCustomEventDialog(
                date = selectedDate.toString(),
                lastEventEndTime = lastEvent?.endTime,
                showMonthViewToggle = true,
                onDismiss = { showAddDialog = false },
                onConfirm = { event ->
                    eventsViewModel.addCustomEvent(event)
                    showAddDialog = false
                }
            )
        }

        if (showEditDialog && selectedEvent != null) {

            val freshEvent = customEvents.find { it.id == selectedEvent!!.id } ?: selectedEvent!!
            EditCustomEventDialog(
                event = freshEvent,
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

                    // ✅ Przywróć zadanie do inbox jeśli pochodziło z zadania
                    event.sourceTaskId?.let { taskId ->
                        tasksViewModel?.assignDateToTask(
                            taskId = taskId,
                            date = null,
                            time = null
                        )
                        android.util.Log.d("CalendarView", "✅ Przywrócono zadanie do inbox: $taskId")
                    }

                    showEditDialog = false
                    selectedEvent = null
                }
            )
        }
        // ✅ NOWY DIALOG: Edycja zadania z siatki
        if (showEditTaskSheet && selectedTask != null) {
            EditTaskSheet(
                task = selectedTask!!,
                onDismiss = {
                    showEditTaskSheet = false
                    selectedTask = null
                },
                onSave = { updatedTask ->
                    tasksViewModel?.updateTask(updatedTask)
                    showEditTaskSheet = false
                    selectedTask = null
                },
                onDelete = { taskToDelete ->
                    tasksViewModel?.deleteTask(taskToDelete)
                    showEditTaskSheet = false
                    selectedTask = null
                }
            )
        }
    }

    @Composable
    fun TimeLabelsColumn() {
        Column(
            modifier = Modifier
                .width(60.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            (0..23).forEach { hour ->
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = String.format("%02d:00", hour),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
    
    @Composable
    fun HourGridLines() {
        Column(modifier = Modifier.fillMaxSize()) {
            (0..23).forEach { _ ->
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }

    @Composable
    fun BoxScope.EventsLayer(
        events: List<CustomEvent>,
        selectedDate: LocalDate,  // ✅ DODAJ TO
        onEventClick: (CustomEvent) -> Unit,
        onEventResizeEnd: (CustomEvent, Int, Int) -> Unit,
        draggedTask: Task? = null,
        draggedHabit: Habit? = null,
        onDropTask: ((Task, Int) -> Unit)? = null,
        onDropHabit: ((Habit, Int) -> Unit)? = null
    ) {
        if (draggedTask != null || draggedHabit != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                (0..23).forEach { hour ->
                    DropZone(
                        hour = hour,
                        onDrop = {
                            draggedTask?.let { task -> onDropTask?.invoke(task, hour) }
                            draggedHabit?.let { habit -> onDropHabit?.invoke(habit, hour) }
                        }
                    )
                }
            }
        }

        events.forEach { event ->
            key(event.id, event.startTime, event.endTime) {
                ResizableEventBlock(
                    event = event,
                    selectedDate = selectedDate,  // ✅ DODAJ TO
                    onClick = { onEventClick(event) },
                    onResizeEnd = { newStart, newEnd ->
                        onEventResizeEnd(event, newStart, newEnd)
                    }
                )
            }
        }
    }
    
    @Composable
    fun DropZone(
        hour: Int,
        onDrop: () -> Unit
    ) {
        var isHovered by remember { mutableStateOf(false) }
    
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    if (isHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else Color.Transparent
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isHovered = true },
                        onDragEnd = {
                            isHovered = false
                            onDrop()
                        },
                        onDragCancel = { isHovered = false },
                        onDrag = { _, _ -> }
                    )
                }
        ) {
            if (isHovered) {
                Text(
                    text = "Upuść tutaj - ${String.format("%02d:00", hour)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }
        }
    }
    
    @Composable
    fun BoxScope.ResizableEventBlock(
        event: CustomEvent,
        selectedDate: LocalDate,
        onClick: () -> Unit,
        onResizeEnd: (Int, Int) -> Unit
    ) {
        var isSelected by remember { mutableStateOf(false) }
        var isDraggingTop by remember { mutableStateOf(false) }
        var isDraggingBottom by remember { mutableStateOf(false) }
        var topOffsetDelta by remember { mutableStateOf(0f) }
        var bottomOffsetDelta by remember { mutableStateOf(0f) }

        val baseStartMinutes = parseTimeToMinutes(event.getStartTimeForDate(selectedDate.toString()))
        val baseEndMinutes = parseTimeToMinutes(event.getEndTimeForDate(selectedDate.toString()))
    
        val adjustedStartMinutes = when {
            isDraggingTop -> {
                (baseStartMinutes + (topOffsetDelta / 2).toInt()).coerceIn(0, baseEndMinutes - 15)
            }
            else -> baseStartMinutes
        }
    
        val adjustedEndMinutes = when {
            isDraggingBottom -> {
                (baseEndMinutes + (bottomOffsetDelta / 2).toInt()).coerceIn(adjustedStartMinutes + 15, 1440)
            }
            else -> baseEndMinutes
        }
    
        val adjustedDuration = adjustedEndMinutes - adjustedStartMinutes
        val baseTopOffset = (adjustedStartMinutes * 2).dp
        val height = (adjustedDuration * 2).dp.coerceAtLeast(40.dp)
    
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(height)
                .offset(y = baseTopOffset)
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .align(Alignment.TopStart)
                .zIndex(if (isSelected) 10f else 1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (isSelected) {
                                // Drugie tapnięcie = otwórz dialog
                                isSelected = false
                                onClick()
                            } else {
                                // Pierwsze tapnięcie = zaznacz (pokaż uchwyty resize)
                                isSelected = true
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(event.color).copy(
                    alpha = if (isDraggingTop || isDraggingBottom) 0.7f else 0.85f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 4.dp else 2.dp
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .align(Alignment.TopCenter)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDraggingTop = true
                                        topOffsetDelta = 0f
                                    },
                                    onDragEnd = {
                                        val finalStartMinutes =
                                            (baseStartMinutes + (topOffsetDelta / 2).toInt())
                                                .coerceIn(0, baseEndMinutes - 15)

                                        onResizeEnd(finalStartMinutes, baseEndMinutes)

                                        topOffsetDelta = 0f
                                        bottomOffsetDelta = 0f
                                        isDraggingTop = false
                                        isSelected = false
                                    },
                                    onDragCancel = {
                                        topOffsetDelta = 0f
                                        isDraggingTop = false
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        topOffsetDelta += dragAmount.y
                                    }
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color.White, CircleShape)
                                .align(Alignment.Center)
                        )
                    }
                }
    
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .padding(
                            top = if (isSelected) 16.dp else 0.dp,
                            bottom = if (isSelected) 16.dp else 0.dp
                        ),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2
                    )
    
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${event.startTime} - ${event.endTime}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
    
                        if (event.hasReminder) {
                            Text("🔔", fontSize = 11.sp)
                        }
                    }
                }
    
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .align(Alignment.BottomCenter)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDraggingBottom = true
                                        bottomOffsetDelta = 0f
                                    },
                                    onDragEnd = {
                                        val finalEndMinutes =
                                            (baseEndMinutes + (bottomOffsetDelta / 2).toInt())
                                                .coerceIn(baseStartMinutes + 15, 1440)

                                        onResizeEnd(baseStartMinutes, finalEndMinutes)

                                        topOffsetDelta = 0f
                                        bottomOffsetDelta = 0f
                                        isDraggingBottom = false
                                        isSelected = false
                                    },
                                    onDragCancel = {
                                        bottomOffsetDelta = 0f
                                        isDraggingBottom = false
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        bottomOffsetDelta += dragAmount.y
                                    }
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color.White, CircleShape)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
    
    fun parseTimeToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    fun minutesToTime(minutes: Int): String {
        val hours = (minutes / 60).coerceIn(0, 23)
        val mins = (minutes % 60).coerceIn(0, 59)
        return String.format("%02d:%02d", hours, mins)
    }

    @Composable
    fun InboxDrawer(
        tasks: List<Task>,
        habits: List<Habit>,
        miniGoals: List<Pair<Goal, MiniGoal>>,
        onTaskDragStart: (Task, Offset) -> Unit,
        onTaskDrag: (Offset) -> Unit,
        onHabitDragStart: (Habit, Offset) -> Unit,
        onHabitDrag: (Offset) -> Unit,
        onMiniGoalDragStart: (Goal, MiniGoal, Offset) -> Unit,
        onMiniGoalDrag: (Offset) -> Unit,
        onMiniGoalDragEnd: () -> Unit,
        onTaskDragEnd: () -> Unit,
        onHabitDragEnd: () -> Unit,
        onClose: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        // ✅ NOWE: Stan rozwinięcia każdej sekcji
        var tasksExpanded by remember { mutableStateOf(true) }
        var habitsExpanded by remember { mutableStateOf(true) }
        var miniGoalsExpanded by remember { mutableStateOf(true) }

        Surface(
            modifier = modifier
                .fillMaxHeight()
                .width(200.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "📋 Inbox",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ✅ SEKCJA ZADANIA - nagłówek klikalny
                if (tasks.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tasksExpanded = !tasksExpanded }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Zadania (${tasks.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (tasksExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (tasksExpanded) {
                        items(tasks, key = { it.id }) { task ->
                            Spacer(modifier = Modifier.height(16.dp))
                            DraggableTaskCard(
                                task = task,
                                onDragStart = { position -> onTaskDragStart(task, position) },
                                onDrag = onTaskDrag,
                                onDragEnd = onTaskDragEnd
                            )
                        }
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                    }
                }

                // ✅ SEKCJA NAWYKI - nagłówek klikalny
                if (habits.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { habitsExpanded = !habitsExpanded }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nawyki (${habits.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (habitsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (habitsExpanded) {
                        items(habits, key = { it.id }) { habit ->
                            Spacer(modifier = Modifier.height(16.dp))
                            DraggableHabitCard(
                                habit = habit,
                                onDragStart = { position -> onHabitDragStart(habit, position) },
                                onDrag = onHabitDrag,
                                onDragEnd = onHabitDragEnd
                            )
                        }
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                    }
                }

                // ✅ SEKCJA MINI-CELE - nagłówek klikalny
                if (miniGoals.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { miniGoalsExpanded = !miniGoalsExpanded }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mini-cele (${miniGoals.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (miniGoalsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (miniGoalsExpanded) {
                        items(miniGoals, key = { (_, miniGoal) -> miniGoal.id }) { (goal, miniGoal) ->
                            Spacer(modifier = Modifier.height(16.dp))
                            DraggableMiniGoalCard(
                                goal = goal,
                                miniGoal = miniGoal,
                                onDragStart = { position -> onMiniGoalDragStart(goal, miniGoal, position) },
                                onDrag = onMiniGoalDrag,
                                onDragEnd = onMiniGoalDragEnd
                            )
                        }
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                    }
                }

                if (tasks.isEmpty() && habits.isEmpty() && miniGoals.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Brak elementów",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DraggableTaskCard(
        task: Task,
        onDragStart: (Offset) -> Unit,
        onDrag: (Offset) -> Unit, // ✅ DODAJ
        onDragEnd: () -> Unit
    ) {
        var isDragging by remember { mutableStateOf(false) }
        var cardPosition by remember { mutableStateOf(Offset.Zero) }
        val cardColor = task.priority.toColor()
    
    
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isDragging) 0.0f else 1f) // ✅ Całkowicie ukryj (było 0.3f)
                .onGloballyPositioned { coordinates ->
                    // ✅ Złap pozycję LEWEGO GÓRNEGO ROGU karty na ekranie
                    val position = coordinates.positionInWindow()
                    cardPosition = position
                    android.util.Log.d(
                        "CardPosition",
                        "Task '${task.title}' positioned at: $position"
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            // ✅ Skoryguj Y - odejmij offset headera/top bara
                            val correctedPosition = Offset(
                                cardPosition.x,
                                cardPosition.y - 431f // Eksperymentuj z wartością (100-150)
                            )
                            onDragStart(correctedPosition)
                            android.util.Log.d(
                                "CardDrag",
                                "Original: $cardPosition, Corrected: $correctedPosition"
                            )
                        },
                        onDragEnd = {
                            isDragging = false
                            onDragEnd()
                        },
                        onDragCancel = {
                            isDragging = false
                            onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // ✅ Przekaż globalną pozycję palca
                            val newPosition = Offset(
                                cardPosition.x + change.position.x,
                                cardPosition.y + change.position.y - 431f
                            )
                            onDrag(newPosition)
                            android.util.Log.d("TaskDrag", "Dragging, new pos: $newPosition")
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = cardColor.copy(alpha = 0.30f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Przeciągnij",
                    tint = cardColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DraggableHabitCard(
        habit: Habit,
        onDragStart: (Offset) -> Unit,
        onDrag: (Offset) -> Unit, // ✅ DODAJ
        onDragEnd: () -> Unit
    ) {
        var isDragging by remember { mutableStateOf(false) }
        var cardPosition by remember { mutableStateOf(Offset.Zero) }
        val inboxGreen = HabitGreen

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isDragging) 0.0f else 1f) // ✅ 0.0f żeby całkowicie zniknęła
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    cardPosition = position
                    android.util.Log.d(
                        "CardPosition",
                        "Habit '${habit.name}' positioned at: $position"
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            val correctedPosition = Offset(
                                cardPosition.x,
                                cardPosition.y - 431f
                            )
                            onDragStart(correctedPosition)
                            android.util.Log.d(
                                "HabitDrag",
                                "Original: $cardPosition, Corrected: $correctedPosition"
                            )
                        },
                        onDragEnd = {
                            isDragging = false
                            onDragEnd()
                        },
                        onDragCancel = {
                            isDragging = false
                            onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newPosition = Offset(
                                cardPosition.x + change.position.x,
                                cardPosition.y + change.position.y - 431f
                            )
                            onDrag(newPosition)
                            android.util.Log.d("HabitDrag", "Dragging, new pos: $newPosition")
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = inboxGreen.copy(alpha = 0.30f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Przeciągnij",
                    tint = inboxGreen,
                    modifier = Modifier.size(16.dp)
                )
    
                Spacer(modifier = Modifier.width(6.dp))
    
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (habit.streak > 0) {
                            Text(
                                text = "🔥${habit.streak}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
    
                        val frequencyText = when {
                            habit.weeklyDays.isNotEmpty() -> "Tyg"
                            habit.monthlyDates.isNotEmpty() -> "Mies"
                            else -> "Codz"
                        }
    
                        Text(
                            text = frequencyText,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    @Composable
    fun DraggableMiniGoalCard(
        goal: Goal,
        miniGoal: MiniGoal,
        onDragStart: (Offset) -> Unit,
        onDrag: (Offset) -> Unit,
        onDragEnd: () -> Unit
    ) {
        var isDragging by remember { mutableStateOf(false) }
        var cardPosition by remember { mutableStateOf(Offset.Zero) }
        val miniGoalColor = GoalGold

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isDragging) 0.0f else 1f)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    cardPosition = position
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            val correctedPosition = Offset(
                                cardPosition.x,
                                cardPosition.y - 431f
                            )
                            onDragStart(correctedPosition)
                        },
                        onDragEnd = {
                            isDragging = false
                            onDragEnd()
                        },
                        onDragCancel = {
                            isDragging = false
                            onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newPosition = Offset(
                                cardPosition.x + change.position.x,
                                cardPosition.y + change.position.y - 431f
                            )
                            onDrag(newPosition)
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = miniGoalColor.copy(alpha = 0.30f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Przeciągnij",
                    tint = miniGoalColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = miniGoal.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    Text(
                        text = "Z: ${goal.title}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    miniGoal.date?.let { date ->
                        Text(
                            text = "📅 $date",
                            fontSize = 9.sp,
                            color = miniGoalColor
                        )
                    }
                }
            }
        }
    }
    @Composable
    fun BoxScope.GoalDeadlineBlock(
        goal: Goal,
        onClick: () -> Unit
    ) {
        android.util.Log.d(
            "GoalDeadline",
            "🏆 Rendering deadline for: ${goal.title}, progress: ${goal.getProgressPercentage()}%"
        )

        val topOffset = (8 * 60) * 2  // 960 dp (8 godzin * 60 minut * 2dp)

        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(100.dp)
                .offset(y = topOffset.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .align(Alignment.TopStart)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = chronosAccentGradient()
                    )
                    .padding(12.dp)  // ✅ Zwiększ padding
            ) {
                Column(  // ✅ ZMIEŃ z Row na Column dla lepszej organizacji
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // ✅ GÓRA: Ikona + "DEADLINE"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)  // ✅ Zmniejsz ikonę
                        )

                        Text(
                            "🏆 DEADLINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // ✅ ŚRODEK: Nazwa celu
                    Text(
                        goal.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    // ✅ DÓŁ: Pasek postępu + procent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ✅ Pasek postępu
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(
                                    Color.White.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(goal.getProgress())  // ✅ 0.0 - 1.0
                                    .background(
                                        Color.White,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // ✅ Procent
                        Text(
                            "${goal.getProgressPercentage()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }




