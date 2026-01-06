import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.add
import androidx.core.util.remove

@androidx.compose.runtime.Composable
fun ChronosNavigation(
    modifier: java.lang.reflect.Modifier = java.lang.reflect.Modifier,
    navController: androidx.navigation.NavHostController,
    habitsList: SnapshotStateList<Habit>,
    tasksList: SnapshotStateList<Task>,
    goalsList: SnapshotStateList<com.google.android.gms.fitness.data.Goal>
) {
    // --- Logika modyfikacji stanu powinna być tutaj ---
    val onAddHabit = { name: String, frequency: String -> habitsList.add(Habit(name = name, frequency = frequency)) }
    val onUpdateHabit = { habit: Habit, completed: Boolean ->
        val index = habitsList.indexOfFirst { it.id == habit.id }
        if (index != -1) habitsList[index] = habit.copy(isCompleted = completed)
    }
    val onDeleteHabit = { habit: Habit -> habitsList.remove(habit) }

    val onAddTask = { name: String -> tasksList.add(Task(name = name)) }
    val onUpdateTask = { task: Task, completed: Boolean ->
        val index = tasksList.indexOfFirst { it.id == task.id }
        if (index != -1) tasksList[index] = task.copy(isCompleted = completed)
    }
    val onDeleteTask = { task: Task -> tasksList.remove(task) }

    val onAddGoal = { name: String -> goalsList.add(com.google.android.gms.fitness.data.Goal(name = name)) }
    val onUpdateGoal = { goal: com.google.android.gms.fitness.data.Goal, completed: Boolean ->
        val index = goalsList.indexOfFirst { it.id == goal.id }
        if (index != -1) goalsList[index] = goal.copy(isCompleted = completed)
    }
    val onDeleteGoal = { goal: com.google.android.gms.fitness.data.Goal -> goalsList.remove(goal) }

    androidx.navigation.NavHost(
        navController = navController,
        startDestination = "menu",
        modifier = modifier
    ) {
        composable("menu") {
            MenuScreen(navController = navController)
        }
        composable("start") {
            StartScreen(
                navController = navController,
                habitsList = habitsList,
                onAddHabit = onAddHabit,
                onUpdateHabit = onUpdateHabit,
                onDeleteHabit = onDeleteHabit
            )
        }
        composable("zadania") {
            ZadaniaScreen(
                navController = navController,
                tasksList = tasksList,
                onAddTask = onAddTask,
                onUpdateTask = onUpdateTask,
                onDeleteTask = onDeleteTask
            )
        }
        composable("cele") {
            CeleScreen(
                navController = navController,
                goalsList = goalsList,
                onAddGoal = onAddGoal,
                onUpdateGoal = onUpdateGoal,
                onDeleteGoal = onDeleteGoal
            )
        }
        composable("options") {
            OptionsScreen(navController = navController)
        }
        composable("exit") {
            ExitScreen(navController = navController)
        }
    }
}