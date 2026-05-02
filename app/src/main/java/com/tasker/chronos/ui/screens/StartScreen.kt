// Plik: ui/screens/StartScreen.kt (KOMPLETNIE POPRAWIONY)
package com.tasker.chronos.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasker.chronos.data.models.*
import com.tasker.chronos.ui.components.*
import com.tasker.chronos.ui.dialogs.AddGoalDialog
import com.tasker.chronos.viewmodels.EventsViewModel
import com.tasker.chronos.viewmodels.TasksViewModel
import com.tasker.chronos.viewmodels.HabitsViewModel
import com.tasker.chronos.viewmodels.GoalsViewModel
import com.tasker.chronos.ui.theme.ChronosMotion
import com.tasker.chronos.ui.theme.textWithBlueGlow
import com.tasker.chronos.ui.theme.textWithGoldGlow
import com.tasker.chronos.ui.theme.textWithGreenGlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    tasksViewModel: TasksViewModel = viewModel(),
    habitsViewModel: HabitsViewModel = viewModel(),
    goalsViewModel: GoalsViewModel = viewModel(),
    eventsViewModel: EventsViewModel = viewModel(),
    initialTab: Int = 0,
    initialGoalId: String? = null,
    initialTaskId: String? = null,
    initialHabitId: String? = null,
    onNavigateToNotes: () -> Unit = {},  // ✅ NOWY PARAMETR
    onNavigateToShopping: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    // ✅ Pobierz dane z ViewModels
    val scheduledTasks by tasksViewModel.scheduledTasks.collectAsState()
    val allActiveTasks by tasksViewModel.allTasks.collectAsState()
    val inboxTasks by tasksViewModel.inboxTasks.collectAsState()
    val habits by habitsViewModel.habits.collectAsState()
    val activeGoals by goalsViewModel.activeGoals.collectAsState()

    // ✅ Filtry
    var taskFilters by remember { mutableStateOf(TaskFilters()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var habitFilters by remember { mutableStateOf(HabitFilters()) }
    var showHabitFilterSheet by remember { mutableStateOf(false) }
    var goalFilters by remember { mutableStateOf(GoalFilters()) }
    var showGoalFilterSheet by remember { mutableStateOf(false) }

    // ✅ Przefiltrowane listy
    val filteredScheduledTasks = remember(scheduledTasks, taskFilters) {
        scheduledTasks.applyFilters(taskFilters)
    }
    val filteredInboxTasks = remember(inboxTasks, taskFilters) {
        inboxTasks.applyFilters(taskFilters)
    }
    val filteredHabits = remember(habits, habitFilters) {
        habits.applyFilters(habitFilters)
    }
    val filteredGoals = remember(activeGoals, goalFilters) {
        activeGoals.applyFilters(goalFilters)
    }

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }

    // ✅ Stan dla dialogów i arkuszy
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedHabit by remember { mutableStateOf<Habit?>(null) }
    var selectedGoal by remember { mutableStateOf<Goal?>(null) }
    var showEditTaskSheet by remember { mutableStateOf(false) }
    var showEditHabitSheet by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var addTaskDialogKey by remember { mutableStateOf(0) }
    var addHabitDialogKey by remember { mutableStateOf(0) }

    // ✅ Toast dla ukończonych elementów
    var completionMessage by remember { mutableStateOf("") }
    var completionType by remember { mutableStateOf(CompletionType.GOAL) }
    var showCompletionToast by remember { mutableStateOf(false) }

    // ✅ Przełącz na tab Cele jeśli przekazano goalId
    LaunchedEffect(initialGoalId) {
        if (initialGoalId != null) {
            selectedTab = 2
        }
    }
    var hasHandledInitialGoal by remember { mutableStateOf(false) }

    LaunchedEffect(initialGoalId) {  // ✅ USUŃ filteredGoals z dependency!
        if (initialGoalId != null && !hasHandledInitialGoal) {
            selectedTab = 2

            // Poczekaj aż filteredGoals będzie gotowy
            val goal = filteredGoals.find { it.id == initialGoalId }
            if (goal != null) {
                selectedGoal = goal
                hasHandledInitialGoal = true  // ✅ Oznacz jako obsłużone
                android.util.Log.d("StartScreen", "✅ Otwarto EditGoalSheet dla: ${goal.title} (z deadline)")
            }
        }
    }

    var hasHandledInitialTask by remember { mutableStateOf(false) }
    LaunchedEffect(initialTaskId, filteredScheduledTasks, filteredInboxTasks) {
        if (initialTaskId.isNullOrBlank() || hasHandledInitialTask) return@LaunchedEffect
        selectedTab = 0
        val task = filteredScheduledTasks.find { it.id == initialTaskId }
            ?: filteredInboxTasks.find { it.id == initialTaskId }
        if (task != null) {
            selectedTask = task
            showEditTaskSheet = true
            hasHandledInitialTask = true
            android.util.Log.d("StartScreen", "✅ Otwarto zadanie z powiadomienia: ${task.title}")
        }
    }

    var hasHandledInitialHabit by remember { mutableStateOf(false) }
    LaunchedEffect(initialHabitId, filteredHabits) {
        if (initialHabitId.isNullOrBlank() || hasHandledInitialHabit) return@LaunchedEffect
        selectedTab = 1
        val habit = filteredHabits.find { it.id == initialHabitId }
        if (habit != null) {
            selectedHabit = habit
            showEditHabitSheet = true
            hasHandledInitialHabit = true
            android.util.Log.d("StartScreen", "✅ Otwarto nawyk z powiadomienia: ${habit.name}")
        }
    }
    // ✅ DODAJ NOWY LaunchedEffect - tuż PRZED istniejącym LaunchedEffect(initialGoalId)
    LaunchedEffect(Unit) {
        goalsViewModel.goalCompletedEvent.collect { goalTitle ->
            completionMessage = "🎉 Cel '$goalTitle' został ukończony w 100%!\n📦 Przeniesiono do archiwum"
            completionType = CompletionType.GOAL
            showCompletionToast = true
        }
    }

    val tabs = listOf("Zadania", "Nawyki", "Cele")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chronos",
                            style = MaterialTheme.typography.headlineMedium.merge(
                                androidx.compose.ui.text.TextStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color(0x4D000000),
                                        offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                                        blurRadius = 12f
                                    )
                                )
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                // ✅ DODAJ TEN KOD PRZED actions:
                navigationIcon = {
                    IconButton(onClick = { onNavigateToNotes() }) {  // ✅ DODAJ CALLBACK
                        Icon(
                            Icons.Default.Note,  // ✅ UŻYJ IKONY NOTATKI
                            contentDescription = "Notatki",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    // ✅ Przycisk filtrów
                    when (selectedTab) {
                        0 -> { // Zadania
                            val activeFiltersCount = listOf(
                                taskFilters.searchQuery.isNotBlank(),
                                taskFilters.priorities.isNotEmpty(),
                                taskFilters.dateRange != null,
                                taskFilters.sortBy != TaskSortOption.DATE_ASC
                            ).count { it }

                            BadgedBox(
                                badge = {
                                    if (activeFiltersCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$activeFiltersCount")
                                        }
                                    }
                                }
                            ) {
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filtruj zadania",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        1 -> { // Nawyki
                            val activeFiltersCount = listOf(
                                habitFilters.searchQuery.isNotBlank(),
                                habitFilters.frequencies.isNotEmpty(),
                                !habitFilters.showCompleted,
                                habitFilters.minStreak != null,
                                habitFilters.sortBy != HabitSortOption.NAME_ASC
                            ).count { it }

                            BadgedBox(
                                badge = {
                                    if (activeFiltersCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$activeFiltersCount")
                                        }
                                    }
                                }
                            ) {
                                IconButton(onClick = { showHabitFilterSheet = true }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filtruj nawyki",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        2 -> { // Cele
                            val activeFiltersCount = listOf(
                                goalFilters.searchQuery.isNotBlank(),
                                goalFilters.progressRange != null,
                                !goalFilters.showCompleted,
                                goalFilters.sortBy != GoalSortOption.DATE_ASC
                            ).count { it }

                            BadgedBox(
                                badge = {
                                    if (activeFiltersCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$activeFiltersCount")
                                        }
                                    }
                                }
                            ) {

                                IconButton(onClick = { showGoalFilterSheet = true }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filtruj cele",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                            }
                        }
                    }
                    IconButton(onClick = { onNavigateToShopping() }) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Zakupy",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                }

            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> addTaskDialogKey++
                        1 -> addHabitDialogKey++
                        2 -> showAddGoalDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Crossfade(targetState = selectedTab, label = "fab_icon_morph") { tab ->
                    when (tab) {
                        1 -> Icon(Icons.Default.Autorenew, contentDescription = "Dodaj nawyk")
                        2 -> Icon(Icons.Default.Flag, contentDescription = "Dodaj cel")
                        else -> Icon(Icons.Default.Check, contentDescription = "Dodaj zadanie")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                transitionSpec = {
                    fadeIn(ChronosMotion.tweenEnter()) togetherWith fadeOut(ChronosMotion.tweenExit())
                },
                label = "start_tab_content"
            ) { tab ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (tab) {
                        0 -> TasksTab(
                            scheduledTasks = filteredScheduledTasks,
                            inboxTasks = filteredInboxTasks,
                            allTasks = allActiveTasks.applyFilters(taskFilters),
                            filters = taskFilters,
                            onTaskClick = { task ->
                                selectedTask = task
                                showEditTaskSheet = true
                            },
                            onTaskCheckedChange = { taskId ->
                                val task = filteredScheduledTasks.find { it.id == taskId }
                                    ?: filteredInboxTasks.find { it.id == taskId }

                                tasksViewModel.toggleTaskCompleted(taskId)

                                if (task != null && !task.isCompleted) {
                                    completionMessage = "Zadanie '${task.title}' zostało ukończone"
                                    completionType = CompletionType.TASK
                                    showCompletionToast = true
                                }
                            }
                            ,
                            onTaskDelete = { taskToDelete ->
                                tasksViewModel.deleteTask(taskToDelete)
                            }
                        )

                        1 -> HabitsTab(
                            habits = filteredHabits,
                            filters = habitFilters,
                            onHabitClick = { habit ->
                                selectedHabit = habit
                                showEditHabitSheet = true
                            },
                            onHabitCheckedChange = { habitId ->
                                habitsViewModel.toggleHabitCompleted(habitId)
                            },
                            onDeleteHabit = { habit ->
                                habitsViewModel.deleteHabit(habit)
                            }
                        )

                        2 -> GoalsTab(
                            goals = filteredGoals,
                            filters = goalFilters,
                            viewModel = goalsViewModel,
                            initialExpandedGoalId = initialGoalId,
                            onGoalClick = { goal ->
                                android.util.Log.d("StartScreen", "🎯 Clicked goal: ${goal.title}")
                                selectedGoal = goal
                            },
                            onGoalCompleted = { goalTitle ->
                                completionMessage = "🎉 Cel '$goalTitle' został ukończony w 100%!\n📦 Przeniesiono do archiwum"
                                completionType = CompletionType.GOAL
                                showCompletionToast = true
                            }
                        )
                    }
                }
            }
        }
    }

    // ✅ Dialogi i arkusze
    if (showFilterSheet) {
        TaskFilterSheet(
            currentFilters = taskFilters,
            onFiltersChanged = { taskFilters = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showHabitFilterSheet) {
        HabitFilterSheet(
            currentFilters = habitFilters,
            onFiltersChanged = { habitFilters = it },
            onDismiss = { showHabitFilterSheet = false }
        )
    }

    if (showGoalFilterSheet) {
        GoalFilterSheet(
            currentFilters = goalFilters,
            onFiltersChanged = { goalFilters = it },
            onDismiss = { showGoalFilterSheet = false }
        )
    }

    if (addTaskDialogKey > 0) {
        key(addTaskDialogKey) {
            AddTaskDialog(
                onDismiss = { addTaskDialogKey = 0 },
                onConfirm = { task ->
                    tasksViewModel.addTask(task)
                    addTaskDialogKey = 0
                }
            )
        }
    }

    if (addHabitDialogKey > 0) {
        key(addHabitDialogKey) {
            AddHabitDialog(
                onDismiss = { addHabitDialogKey = 0 },
                onConfirm = { habit ->
                    habitsViewModel.addHabit(habit)
                    addHabitDialogKey = 0
                }
            )
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { goal ->
                goalsViewModel.addGoal(goal)
                showAddGoalDialog = false
            }
        )
    }

    AnimatedSheetHost(visible = showEditTaskSheet && selectedTask != null) {
        EditTaskSheet(
            task = selectedTask!!,
            onDismiss = {
                showEditTaskSheet = false
                selectedTask = null
            },
            onSave = { updatedTask ->
                tasksViewModel.updateTask(updatedTask)
                showEditTaskSheet = false
                selectedTask = null
            },
            onDelete = { taskToDelete ->
                tasksViewModel.deleteTask(taskToDelete)
                showEditTaskSheet = false
                selectedTask = null
            }
        )
    }

    AnimatedSheetHost(visible = showEditHabitSheet && selectedHabit != null) {
        EditHabitSheet(
            habit = selectedHabit!!,
            onDismiss = {
                showEditHabitSheet = false
                selectedHabit = null
            },
            onSave = { updatedHabit ->
                habitsViewModel.updateHabit(updatedHabit)
                showEditHabitSheet = false
                selectedHabit = null
            },
            onDelete = { habitToDelete ->
                habitsViewModel.deleteHabit(habitToDelete)
                showEditHabitSheet = false
                selectedHabit = null
            }
        )
    }

    if (selectedGoal != null) {
        val goalSnapshot = selectedGoal!!
        val currentGoal = filteredGoals.find { it.id == goalSnapshot.id }
        val currentGoalId = currentGoal?.id ?: goalSnapshot.id

        // ✅ DODAJ: Reset selectedGoal po zamknięciu arkusza
        DisposableEffect(goalSnapshot.id) {
            onDispose {
                android.util.Log.d("StartScreen", "🔄 DisposableEffect - czyszczę selectedGoal")
                selectedGoal = null
            }
        }


        AnimatedSheetHost(visible = currentGoal != null) {
            EditGoalSheet(
                goal = currentGoal ?: goalSnapshot,
                onDismiss = {
                    android.util.Log.d("StartScreen", "❌ onDismiss - zamykam arkusz")
                    selectedGoal = null
                },
                onSave = { updatedGoal ->
                    goalsViewModel.updateGoal(updatedGoal)
                    selectedGoal = null
                },
                onDelete = { goalToDelete ->
                    android.util.Log.d("EditGoalSheet", "🗑️ Usuwam cel: ${goalToDelete.title}")
                    android.util.Log.d("EditGoalSheet", "📊 isCompleted: ${goalToDelete.isCompleted()}")

                    goalsViewModel.deleteGoal(goalToDelete, eventsViewModel)  // ✅ PRZEKAŻ eventsViewModel!
                    selectedGoal = null
                },
                onToggleMiniGoal = { miniGoalId ->
                    goalsViewModel.toggleMiniGoalCompleted(currentGoalId, miniGoalId)
                },
                onAddMiniGoal = { miniGoal ->
                    goalsViewModel.addMiniGoal(currentGoalId, miniGoal)
                },
                onDeleteMiniGoal = { miniGoalId ->
                    goalsViewModel.deleteMiniGoal(currentGoalId, miniGoalId)
                },
                onUpdateMiniGoal = { miniGoal ->
                    goalsViewModel.updateMiniGoal(currentGoalId, miniGoal)
                },
                onGoalCompleted = { title ->
                    completionMessage = "Cel '$title' został ukończony w 100%"
                    completionType = CompletionType.GOAL
                    showCompletionToast = true
                }
            )
        }
        if (currentGoal == null) {
                selectedGoal = null
            }
        }


    if (showCompletionToast) {
        CompletionToast(
            message = completionMessage,  // ✅ To działa poprawnie
            type = completionType,
            onDismiss = { showCompletionToast = false }
        )
    }
}

@Composable
private fun AnimatedSheetHost(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
            expandIn(
                expandFrom = Alignment.Center,
                initialSize = { IntSize((it.width * 0.92f).toInt(), (it.height * 0.92f).toInt()) },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        exit = fadeOut() + shrinkOut() + scaleOut(targetScale = 0.96f)
    ) {
        content()
    }
}

// ✅ POZOSTAŁE FUNKCJE (TasksTab, HabitsTab, GoalsTab, HabitItem)
// Skopiuj je z poprzedniego kodu - są poprawne

@Composable
fun HabitsTab(
    habits: List<Habit>,
    filters: HabitFilters,
    onHabitClick: (Habit) -> Unit,
    onHabitCheckedChange: (String) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    val hasActiveFilters = filters.searchQuery.isNotBlank() ||
            filters.frequencies.isNotEmpty() ||
            !filters.showCompleted

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasActiveFilters) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Text("Filtry aktywne (${habits.size} nawyków)")
                }
            }
        }

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Brak nawyków")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "🌿 Nawyki (${habits.size})",
                        style = MaterialTheme.typography.titleMedium.merge(textWithGreenGlow()),
                        fontWeight = FontWeight.Bold
                    )
                }
                items(habits, key = { it.id }) { habit ->
                    HabitItem(
                        habit = habit,
                        onHabitClick = { onHabitClick(habit) },
                        onHabitCheckedChange = onHabitCheckedChange,
                        onDeleteHabit = onDeleteHabit
                    )
                }
            }
        }
    }
}

@Composable
fun GoalsTab(
    goals: List<Goal>,
    filters: GoalFilters,
    viewModel: GoalsViewModel,
    initialExpandedGoalId: String? = null,
    onGoalClick: (Goal) -> Unit,
    onGoalCompleted: (String) -> Unit = {}
) {
    val hasActiveFilters = filters.searchQuery.isNotBlank() ||
            filters.progressRange != null ||
            !filters.showCompleted

    var expandedGoalId by remember { mutableStateOf(initialExpandedGoalId) }
    val listState = rememberLazyListState()

    LaunchedEffect(initialExpandedGoalId) {
        if (initialExpandedGoalId != null) {
            expandedGoalId = initialExpandedGoalId
            val index = goals.indexOfFirst { it.id == initialExpandedGoalId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasActiveFilters) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Text("Filtry aktywne (${goals.size} celów)")
                }
            }
        }

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hasActiveFilters) "Brak celów spełniających kryteria" else "Brak celów",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "🏆 Cele (${goals.size})",
                        style = MaterialTheme.typography.titleMedium.merge(textWithGoldGlow()),
                        fontWeight = FontWeight.Bold
                    )
                }
                items(goals, key = { it.id }) { goal ->
                    // ✅ NOWY KOD:
                    GoalItem(
                        goal = goal,
                        onGoalClick = {
                            android.util.Log.d("GoalsTab", "🎯 onGoalClick wywołany dla: ${goal.title}")
                            onGoalClick(goal)
                        },
                        onToggleGoalCompleted = { goalId ->
                            android.util.Log.d("GoalsTab", "✅ toggleGoalCompleted: $goalId")

                            // ✅ DODAJ SPRAWDZENIE:
                            val goalToToggle = goals.find { it.id == goalId }
                            if (goalToToggle != null) {
                                val wasIncomplete = !goalToToggle.isCompleted()
                                viewModel.toggleGoalCompleted(goalId)

                                // ✅ PoToggleGoalCompleted sprawdź czy ukończony
                                if (wasIncomplete) {
                                    // Pobierz zaktualizowany cel z ViewModelu
                                    kotlinx.coroutines.GlobalScope.launch {
                                        kotlinx.coroutines.delay(100) // Poczekaj na aktualizację
                                        val updatedGoal = viewModel.allGoals.value.find { it.id == goalId }
                                        if (updatedGoal != null && updatedGoal.isCompleted()) {
                                            onGoalCompleted(updatedGoal.title)
                                        }
                                    }
                                }
                            }
                        },
                        onToggleMiniGoal = { miniGoalId ->
                            android.util.Log.d("GoalsTab", "🔘 toggleMiniGoal: $miniGoalId w celu: ${goal.id}")
                            viewModel.toggleMiniGoalCompleted(goal.id, miniGoalId)
                        },
                        isExpanded = expandedGoalId == goal.id,
                        onExpandToggle = {
                            expandedGoalId = if (expandedGoalId == goal.id) null else goal.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onHabitClick: () -> Unit,
    onHabitCheckedChange: (String) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    val today = java.time.LocalDate.now()
    val isCompletedToday = habit.completionDates.contains(today.toString())
    var showExplosion by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val checkScale by animateFloatAsState(
        targetValue = if (showExplosion) 1.35f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "habit_check_scale"
    )

    val isActiveToday = when {
        habit.weeklyDays.isNotEmpty() -> habit.weeklyDays.contains(today.dayOfWeek.name)
        habit.monthlyDates.isNotEmpty() -> habit.monthlyDates.any { it.toIntOrNull() == today.dayOfMonth }
        else -> true
    }

    SwipeToDismissBox(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        state = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        if (!isCompletedToday && isActiveToday) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showExplosion = true
                            onHabitCheckedChange(habit.id)
                        }
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        onDeleteHabit(habit)
                        true
                    }
                    SwipeToDismissBoxValue.Settled -> false
                }
            },
            positionalThreshold = { it * 0.3f }
        ),
        backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
            ) {}
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        Box {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onHabitClick,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompletedToday)
                        Color(0xFF4CAF50).copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCompletedToday,
                        onCheckedChange = {
                            if (isActiveToday) {
                                if (!isCompletedToday) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showExplosion = true
                                }
                                onHabitCheckedChange(habit.id)
                            }
                        },
                        enabled = isActiveToday,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .scale(checkScale)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy( // ✅ titleMedium
                        textDecoration = if (isCompletedToday)
                            androidx.compose.ui.text.style.TextDecoration.LineThrough
                        else null,
                        fontWeight = FontWeight.SemiBold // ✅ Pogrubienie
                    ),
                    color = if (isCompletedToday)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp)) // ✅ Większy odstęp

                // Szczegóły w boxach
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Typ częstotliwości
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when {
                                habit.weeklyDays.isNotEmpty() -> "📅 Tygodniowo"
                                habit.monthlyDates.isNotEmpty() -> "📆 Miesięcznie"
                                else -> "🔄 Codziennie"
                            },
                            style = MaterialTheme.typography.labelMedium, // ✅ labelMedium
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Streak
                    if (habit.streak > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF6B35).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🔥 ${habit.streak}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = Color(0xFFFF6B35),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Przypomnienie
                    if (habit.hasReminder && habit.reminderTime != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "⏰ ${habit.reminderTime}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Punkty
                if (habit.points > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⭐ +${habit.points} pkt",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status nieaktywny
                if (!isActiveToday) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💤 Nieaktywny dzisiaj",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Red.copy(alpha = 0.7f)
                    )
                }
                    }
                }
            }

            ParticleExplosion(
                isExploding = showExplosion,
                onComplete = { showExplosion = false }
            )
        }
    }
}
@Composable
fun TasksTab(
    scheduledTasks: List<Task>,
    inboxTasks: List<Task>,
    allTasks: List<Task>,
    filters: TaskFilters,
    onTaskClick: (Task) -> Unit,
    onTaskCheckedChange: (String) -> Unit,
    onTaskDelete: (Task) -> Unit
) {
    val allEmpty = allTasks.isEmpty()
    val hasActiveFilters = filters.searchQuery.isNotBlank() ||
            filters.priorities.isNotEmpty() ||
            filters.dateRange != null

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasActiveFilters) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Filtry aktywne (${allTasks.size} zadań)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (allEmpty) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (hasActiveFilters) Icons.Default.SearchOff else Icons.Default.TaskAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (hasActiveFilters) "Brak zadań spełniających kryteria" else "Brak zadań",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (inboxTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "📥 Bez daty (${inboxTasks.size})",
                            style = MaterialTheme.typography.titleMedium.merge(textWithBlueGlow()),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(inboxTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onTaskCheckedChange = onTaskCheckedChange,
                            onTaskClick = { onTaskClick(task) },
                            onTaskLongClick = { onTaskClick(task) },
                            onDeleteTask = onTaskDelete,
                            showDate = false
                        )
                    }
                }

                if (scheduledTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "📅 Zaplanowane (${scheduledTasks.size})",
                            style = MaterialTheme.typography.titleMedium.merge(textWithBlueGlow()),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(scheduledTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onTaskCheckedChange = onTaskCheckedChange,
                            onTaskClick = { onTaskClick(task) },
                            onTaskLongClick = { onTaskClick(task) },
                            onDeleteTask = onTaskDelete,
                            showDate = true
                        )
                    }
                }
            }
        }
    }
}

