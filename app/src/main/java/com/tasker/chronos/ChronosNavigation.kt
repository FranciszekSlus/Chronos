// Plik: navigation/ChronosNavigation.kt
package com.tasker.chronos.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.tasker.chronos.ui.screens.ArchiveScreen
import com.tasker.chronos.ui.screens.CalendarScreen
import com.tasker.chronos.ui.screens.GoalDetailsScreen
import com.tasker.chronos.ui.screens.NotesScreen
import com.tasker.chronos.ui.screens.SettingsScreen
import com.tasker.chronos.ui.screens.ShoppingScreen
import com.tasker.chronos.ui.screens.StartScreen
import com.tasker.chronos.ui.screens.StatisticsScreen
import com.tasker.chronos.viewmodels.CalendarViewModel
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
    shoppingViewModel: ShoppingViewModel = viewModel()

) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Calendar, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Podstawowa trasa Home (bez parametrów)
            // Podstawowa trasa Home
            composable(Screen.Home.route) {
                StartScreen(
                    habitsViewModel = habitsViewModel,
                    tasksViewModel = tasksViewModel,
                    goalsViewModel = goalsViewModel,
                    initialTab = 0,
                    initialGoalId = null,
                    onNavigateToNotes = {  // ✅ DODAJ
                        navController.navigate("notes")
                    },
                    onNavigateToShopping = {  // ✅ DODAJ
                        navController.navigate("shopping")
                    }
                )
            }

// Home z parametrami
            composable(
                route = "home/{tabIndex}/{goalId}",
                arguments = listOf(
                    navArgument("tabIndex") {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                    navArgument("goalId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
                val goalId = backStackEntry.arguments?.getString("goalId")

                StartScreen(
                    habitsViewModel = habitsViewModel,
                    tasksViewModel = tasksViewModel,
                    goalsViewModel = goalsViewModel,
                    initialTab = tabIndex,
                    initialGoalId = goalId,
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
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Calendar.route) {
                val eventsViewModel: EventsViewModel = viewModel()
                val tasksViewModel: TasksViewModel = viewModel()
                val habitsViewModel: HabitsViewModel = viewModel()
                val calendarViewModel: CalendarViewModel = viewModel()
                val goalsViewModel: GoalsViewModel = viewModel()  // ✅ CZY TO JEST?

                CalendarScreen(
                    calendarViewModel = calendarViewModel,
                    habitsViewModel = habitsViewModel,
                    onNavigateToGoal = { goalId ->
                        navController.navigate("home/2/$goalId") {
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                        }
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