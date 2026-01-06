// Plik: ui/screens/StatisticsScreen.kt
package com.tasker.chronos.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.viewmodels.HabitsViewModel
import com.tasker.chronos.viewmodels.TasksViewModel
import com.tasker.chronos.viewmodels.GoalsViewModel
import com.tasker.chronos.viewmodels.UserProfileViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

// Zastąp początek StatisticsScreen tym kodem:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    habitsViewModel: HabitsViewModel,
    tasksViewModel: TasksViewModel,
    goalsViewModel: GoalsViewModel,
    userProfileViewModel: UserProfileViewModel,
    onNavigateBack: () -> Unit = {} // ✅ DODAJ
) {
    // POPRAWKA: Poprawne pobieranie danych
    val habits by habitsViewModel.habits.collectAsState()
    val allTasks by tasksViewModel.allTasks.collectAsState()
    val archivedTasks by tasksViewModel.archivedTasks.collectAsState()
    val activeGoals by goalsViewModel.activeGoals.collectAsState()
    val archivedGoals by goalsViewModel.archivedGoals.collectAsState() // ✅ Zmiana nazwy
    val userProfile by userProfileViewModel.userProfile.collectAsState()

    // Oblicz statystyki
    val totalHabits = habits.size
    val activeStreaks = habits.count { habit -> habit.streak > 0 } // POPRAWKA: Jawny parametr
    val totalTasks = allTasks.size
    val completedTasksCount = archivedTasks.size
    val totalGoals = activeGoals.size
    val completedGoalsCount = archivedGoals.size // ✅ Użyj archivedGoals
    val longestStreak = habits.maxOfOrNull { it.streak } ?: 0 // POPRAWKA: Dodano domyślną wartość

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statystyki 📊", fontWeight = FontWeight.Bold) },
                navigationIcon = { // ✅ DODAJ
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Punkty z animacją
            AnimatedPointsCard(points = userProfile.totalPoints)

            // Statystyki główne
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Nawyki",
                    value = totalHabits.toString(),
                    subtitle = "$activeStreaks aktywnych",
                    icon = Icons.Default.FitnessCenter,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Zadania",
                    value = completedTasksCount.toString(),
                    subtitle = "ukończonych",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Cele",
                    value = totalGoals.toString(),
                    subtitle = "$completedGoalsCount ukończonych",
                    icon = Icons.Default.EmojiEvents,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Streak",
                    value = longestStreak.toString(),
                    subtitle = "najdłuższy",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFFF6B35),
                    modifier = Modifier.weight(1f)
                )
            }

            // Wykres nawyków (ostatnie 7 dni)
            HabitWeekChart(habits = habits)

            // Heatmap nawyków (ostatnie 30 dni)
            HabitHeatmap(habits = habits)

            // Wykres kołowy postępu celów
            GoalsProgressChart(goals = activeGoals)
        }
    }
}

@Composable
fun AnimatedPointsCard(points: Int) {
    var animatedPoints by remember { mutableStateOf(0) }

    LaunchedEffect(points) {
        val step = if (points > 100) 5 else 1
        var current = 0
        while (current < points) {
            current = (current + step).coerceAtMost(points)
            animatedPoints = current
            delay(20)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.3f),
                            Color(0xFFFF6B35).copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = animatedPoints.toString(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Łączna liczba punktów",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )

                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun HabitWeekChart(habits: List<com.tasker.chronos.data.models.Habit>) {
    val today = LocalDate.now()
    val last7Days = (0..6).map { today.minusDays(it.toLong()) }.reversed()

    // Policz ukończone nawyki dla każdego dnia
    val completionCounts = last7Days.map { date ->
        habits.count { habit ->
            habit.completionDates.contains(date.toString())
        }
    }

    val maxCount = completionCounts.maxOrNull() ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Aktywność w ostatnim tygodniu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                completionCounts.forEachIndexed { index, count ->
                    AnimatedBar(
                        value = count,
                        maxValue = maxCount,
                        label = last7Days[index].format(DateTimeFormatter.ofPattern("EEE")),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBar(
    value: Int,
    maxValue: Int,
    label: String,
    color: Color
) {
    var animatedHeight by remember { mutableStateOf(0f) }
    val targetHeight = if (maxValue > 0) (value.toFloat() / maxValue) else 0f

    LaunchedEffect(targetHeight) {
        delay(200)
        var current = 0f
        while (current < targetHeight) {
            current = (current + 0.05f).coerceAtMost(targetHeight)
            animatedHeight = current
            delay(20)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight(animatedHeight)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(color)
            )
        }

        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun HabitHeatmap(habits: List<com.tasker.chronos.data.models.Habit>) {
    val today = LocalDate.now()
    val last30Days = (0..29).map { today.minusDays(it.toLong()) }.reversed()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Heatmap nawyków (30 dni)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Siatka 6x5 (30 dni)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                last30Days.chunked(6).forEach { week ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        week.forEach { date ->
                            val count = habits.count { habit ->
                                habit.completionDates.contains(date.toString())
                            }

                            HeatmapCell(count = count, date = date)
                        }
                    }
                }
            }

            // Legenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mniej", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HeatmapCell(count = 0, date = today)
                HeatmapCell(count = 2, date = today)
                HeatmapCell(count = 4, date = today)
                HeatmapCell(count = 6, date = today)
                Text("Więcej", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun HeatmapCell(count: Int, date: LocalDate) {
    val color = when {
        count == 0 -> Color(0xFFEEEEEE)
        count <= 2 -> Color(0xFFC8E6C9)
        count <= 4 -> Color(0xFF81C784)
        else -> Color(0xFF4CAF50)
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}

@Composable
fun GoalsProgressChart(goals: List<com.tasker.chronos.data.models.Goal>) {
    if (goals.isEmpty()) return

    val completedGoals = goals.count { it.isCompleted() }
    val inProgressGoals = goals.size - completedGoals
    val totalProgress = goals.sumOf { it.getProgressPercentage() } / goals.size.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Postęp celów",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wykres kołowy
                AnimatedPieChart(
                    completed = completedGoals,
                    inProgress = inProgressGoals
                )

                // Statystyki
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendItem(
                        color = Color(0xFF4CAF50),
                        label = "Ukończone",
                        value = completedGoals.toString()
                    )

                    LegendItem(
                        color = Color(0xFFFF9800),
                        label = "W trakcie",
                        value = inProgressGoals.toString()
                    )

                    Divider()

                    Text(
                        "Średni postęp: ${totalProgress.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedPieChart(completed: Int, inProgress: Int) {
    val total = completed + inProgress
    if (total == 0) return

    val completedAngle = (completed.toFloat() / total) * 360f
    var animatedAngle by remember { mutableStateOf(0f) }

    LaunchedEffect(completedAngle) {
        var current = 0f
        while (current < completedAngle) {
            current = (current + 5f).coerceAtMost(completedAngle)
            animatedAngle = current
            delay(20)
        }
    }

    Canvas(
        modifier = Modifier.size(120.dp)
    ) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // W trakcie (pełne koło)
        drawArc(
            color = Color(0xFFFF9800),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        // Ukończone (nakładka)
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = -90f,
            sweepAngle = animatedAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            "$label: $value",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}