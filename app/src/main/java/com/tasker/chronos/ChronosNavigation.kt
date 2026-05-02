// Plik: navigation/ChronosNavigation.kt
package com.tasker.chronos.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tasker.chronos.ui.theme.ChronosMotion
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import com.tasker.chronos.ui.screens.AchievementsScreen
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.tasker.chronos.ui.components.calendar.CalendarDayView
import com.tasker.chronos.ui.screens.ArchiveScreen
import com.tasker.chronos.ui.screens.CalendarScreen
import com.tasker.chronos.ui.screens.DayScheduleScreen
import com.tasker.chronos.ui.screens.GoalDetailsScreen
import com.tasker.chronos.ui.screens.NotesScreen
import com.tasker.chronos.ui.screens.SettingsScreen
import com.tasker.chronos.ui.screens.ShoppingScreen
import com.tasker.chronos.ui.screens.StartScreen
import com.tasker.chronos.ui.screens.StatisticsScreen
import com.tasker.chronos.viewmodels.CalendarViewModel
import com.tasker.chronos.viewmodels.DayScheduleViewModel
import com.tasker.chronos.viewmodels.EventsViewModel
import com.tasker.chronos.viewmodels.GoalsViewModel
import com.tasker.chronos.viewmodels.HabitsViewModel
import com.tasker.chronos.viewmodels.NotesViewModel
import com.tasker.chronos.viewmodels.TasksViewModel
import com.tasker.chronos.viewmodels.UserProfileViewModel
import com.tasker.chronos.viewmodels.SettingsViewModel
import com.tasker.chronos.viewmodels.ShoppingViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Calendar : Screen("calendar", "Kalendarz", Icons.Default.CalendarToday)
    object Settings : Screen("settings", "Ustawienia", Icons.Default.Settings)
}

private fun bottomNavOrder(route: String?): Int? {
    return when (route) {
        Screen.Home.route -> 0
        Screen.Calendar.route -> 1
        Screen.Settings.route -> 2
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronosNavigation(
    habitsViewModel: HabitsViewModel = viewModel(),
    tasksViewModel: TasksViewModel = viewModel(),
    goalsViewModel: GoalsViewModel = viewModel(),
    eventsViewModel: EventsViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    notesViewModel: NotesViewModel = viewModel(),  // ✅ NOWY
    shoppingViewModel: ShoppingViewModel = viewModel(),
    dayScheduleViewModel: DayScheduleViewModel = viewModel(),
    notificationTarget: NotificationNavigationTarget? = null
    ) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Calendar, Screen.Settings)
    var pendingHomeTarget by remember { mutableStateOf<NotificationNavigationTarget?>(notificationTarget) }
    var calendarDeepLink by remember {
        mutableStateOf<Triple<String?, String?, Long>?>(null)
    }
    var lastHandledNonce by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(notificationTarget?.nonce) {
        val target = notificationTarget ?: return@LaunchedEffect
        if (lastHandledNonce == target.nonce) return@LaunchedEffect
        lastHandledNonce = target.nonce

        if (target.openCalendar) {
            pendingHomeTarget = null
            calendarDeepLink = Triple(
                target.calendarEventId,
                target.calendarEventDate,
                target.nonce
            )
            navController.navigate(Screen.Calendar.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            calendarDeepLink = null
            pendingHomeTarget = target
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val selectedIndex = items.indexOfFirst { screen ->
                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                }.coerceAtLeast(0)
                val animatedSelectedIndex by animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "bottom_nav_pill_position"
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    val tabWidth = maxWidth / items.size
                    val animatedOffset = tabWidth * animatedSelectedIndex

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 8.dp)
                            .width((tabWidth - 16.dp).coerceAtLeast(0.dp))
                            .height(32.dp)
                            .offset(x = animatedOffset)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(18.dp)
                            )
                    )

                    Row(modifier = Modifier.fillMaxSize()) {
                        items.forEach { screen ->
                            val selected =
                                currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clickable {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = screen.title,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            enterTransition = {
                val from = bottomNavOrder(initialState.destination.route)
                val to = bottomNavOrder(targetState.destination.route)
                if (from != null && to != null) {
                    if (to > from) {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = ChronosMotion.tweenEnter()
                        ) + fadeIn(ChronosMotion.tweenEnter())
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = ChronosMotion.tweenEnter()
                        ) + fadeIn(ChronosMotion.tweenEnter())
                    }
                } else {
                    fadeIn(ChronosMotion.tweenEnter()) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = ChronosMotion.tweenEnter()
                    )
                }
            },
            exitTransition = {
                val from = bottomNavOrder(initialState.destination.route)
                val to = bottomNavOrder(targetState.destination.route)
                if (from != null && to != null) {
                    if (to > from) {
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = ChronosMotion.tweenExit()
                        ) + fadeOut(ChronosMotion.tweenExit())
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = ChronosMotion.tweenExit()
                        ) + fadeOut(ChronosMotion.tweenExit())
                    }
                } else {
                    fadeOut(ChronosMotion.tweenExit()) + scaleOut(
                        targetScale = 1.02f,
                        animationSpec = ChronosMotion.tweenExit()
                    )
                }
            },
            popEnterTransition = {
                val from = bottomNavOrder(initialState.destination.route)
                val to = bottomNavOrder(targetState.destination.route)
                if (from != null && to != null) {
                    if (to > from) {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = ChronosMotion.tweenEnter()
                        ) + fadeIn(ChronosMotion.tweenEnter())
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = ChronosMotion.tweenEnter()
                        ) + fadeIn(ChronosMotion.tweenEnter())
                    }
                } else {
                    fadeIn(ChronosMotion.tweenEnter()) + scaleIn(
                        initialScale = 1.02f,
                        animationSpec = ChronosMotion.tweenEnter()
                    )
                }
            },
            popExitTransition = {
                val from = bottomNavOrder(initialState.destination.route)
                val to = bottomNavOrder(targetState.destination.route)
                if (from != null && to != null) {
                    if (to > from) {
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = ChronosMotion.tweenExit()
                        ) + fadeOut(ChronosMotion.tweenExit())
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = ChronosMotion.tweenExit()
                        ) + fadeOut(ChronosMotion.tweenExit())
                    }
                } else {
                    fadeOut(ChronosMotion.tweenExit()) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = ChronosMotion.tweenExit()
                    )
                }
            }
        ) {
            // Podstawowa trasa Home (bez parametrów)
            // Podstawowa trasa Home
            composable(Screen.Home.route) {
                StartScreen(
                    habitsViewModel = habitsViewModel,
                    tasksViewModel = tasksViewModel,
                    goalsViewModel = goalsViewModel,
                    initialTab = pendingHomeTarget?.tabIndex ?: 0,
                    initialGoalId = pendingHomeTarget?.goalId,
                    initialTaskId = pendingHomeTarget?.taskId,
                    initialHabitId = pendingHomeTarget?.habitId,
                    onNavigateToNotes = {  // ✅ DODAJ
                        navController.navigate("notes")
                    },
                    onNavigateToShopping = {  // ✅ DODAJ
                        navController.navigate("shopping")
                    }
                )
            }

            composable(
                route = "goal_details/{goalId}",
                arguments = listOf(navArgument("goalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getString("goalId")
                val goals by goalsViewModel.allGoals.collectAsState() // Użyj collectAsState()
                val goal = goals.find { it.id == goalId }

                if (goal != null) {
                    GoalDetailsScreen(
                        goal = goal,
                        goalsViewModel = goalsViewModel,
                        eventsViewModel = eventsViewModel,  // ✅ DODAJ
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Calendar.route) {
                val eventsViewModel: EventsViewModel = viewModel()
                val habitsViewModel: HabitsViewModel = viewModel()
                val calendarViewModel: CalendarViewModel = viewModel()
                val goalsViewModel: GoalsViewModel = viewModel()  // ✅ CZY TO JEST?
                val link = calendarDeepLink

                CalendarScreen(
                    calendarViewModel = calendarViewModel,
                    habitsViewModel = habitsViewModel,
                    tasksViewModel = tasksViewModel,
                    deepLinkEventId = link?.first,
                    deepLinkEventDate = link?.second,
                    deepLinkNonce = link?.third,
                    onCalendarDeepLinkHandled = {
                        if (calendarDeepLink?.third == link?.third) {
                            calendarDeepLink = null
                        }
                    },
                    onNavigateToGoal = { goalId ->
                        pendingHomeTarget = NotificationNavigationTarget(tabIndex = 2, goalId = goalId)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSchedules = { date ->  // ✅ DODAJ
                        navController.navigate("day_schedules/$date")
                    }
                )
            }


            composable(Screen.Settings.route) {
                SettingsScreen(
                    userProfileViewModel = userProfileViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateToArchive = {
                        navController.navigate("archive")
                    },
                    onNavigateToHelp = {
                        // TODO: Nawigacja do pomocy
                    },
                    onNavigateToStatistics = {
                        navController.navigate("statistics")
                    },
                            onNavigateToAchievements = {
                        navController.navigate("achievements")
                    }
                )
            }
            // ✅ NOWA TRASA: Notatki
            composable("notes") {
                NotesScreen(
                    notesViewModel = notesViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

// ✅ NOWA TRASA: Zakupy
            composable("shopping") {
                ShoppingScreen(
                    shoppingViewModel = shoppingViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("statistics") {
                StatisticsScreen(
                    habitsViewModel = habitsViewModel,
                    tasksViewModel = tasksViewModel,
                    goalsViewModel = goalsViewModel,
                    userProfileViewModel = userProfileViewModel,
                    onNavigateBack = { // ✅ DODAJ
                        navController.popBackStack()
                    }
                )
            }
            composable("archive") {
                ArchiveScreen(
                    tasksViewModel = tasksViewModel,
                    goalsViewModel = goalsViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("achievements") {
                AchievementsScreen(
                    userProfileViewModel = userProfileViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "day_schedules/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: ""
                DayScheduleScreen(
                    dayScheduleViewModel = dayScheduleViewModel,
                    eventsViewModel = eventsViewModel,
                    selectedDate = date,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun CalendarPlaceholder() {
    Surface(
        modifier = Modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = "🗓️ Kalendarz - wkrótce!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(32.dp)
        )
    }
}

//Zr0bmYTestY123