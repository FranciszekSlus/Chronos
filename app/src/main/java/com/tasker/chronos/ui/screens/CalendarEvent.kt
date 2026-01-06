package com.tasker.chronos.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tasker.chronos.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.tasker.chronos.ui.dialogs.AddEventDialog
import com.tasker.chronos.viewmodels.EventsViewModel
import com.tasker.chronos.viewmodels.HabitsViewModel

@Composable
fun CalendarScreen(
    navController: NavController,
    onDateSelected: (String) -> Unit,
    eventsViewModel: EventsViewModel, // Zamiast calendarViewModel
    habitsViewModel: HabitsViewModel
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<String?>(null) } // Zapamiętaj wybraną datę


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E3440)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "KALENDARZ",
            fontSize = 28.sp,
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.blackops))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- ZMIANA: Dodanie animacji ładowania ---
        // W przyszłości możesz owinąć to w warunek, np. if (isLoading) { ... }
        LoadingAnimation()

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("todaySchedule") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)
        ) {
            Text(
                "Dzisiaj",
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.blackops)),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Nagłówek miesiąca z przyciskami nawigacji
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Text("<")
            }

            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy")),
                fontSize = 20.sp,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.blackops))
            )

            Button(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Siatka kalendarza
        CalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate, // Przekaż wybraną datę
            onDateClick = { date ->
                selectedDate = date // Zaznacz wybrany dzień
                onDateSelected(date)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.navigate("menu") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text("Powrót", color = Color.Black, fontFamily = FontFamily(Font(R.font.blackops)))
        }
    }

    // PRZYCISK + W PRAWYM DOLNYM ROGU
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { showAddEventDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(30.dp)
                .size(60.dp)
        ) {
            Text(
                "+",
                color = Color.Black,
                fontSize = 26.sp,
                fontFamily = FontFamily(Font(R.font.blackops))
            )
        }

    }

    // DIALOG DODAWANIA WYDARZENIA
    if (showAddEventDialog) {
        // Konwertujemy String z 'selectedDate' na obiekt LocalDate.
        // Jeśli nic nie jest zaznaczone (selectedDate jest null), używamy dzisiejszej daty.
        val initialDialogDate = selectedDate?.let { LocalDate.parse(it) } ?: LocalDate.now()

        AddEventDialog(
            initialDate = initialDialogDate.toString(),
            onDismiss = { showAddEventDialog = false },
            onConfirm = { newEvent ->
                // W przyszłości, gdy podłączymy tu ViewModel, będziemy wywoływać:
                // eventsViewModel.addEvent(newEvent)
                showAddEventDialog = false
            }
        )
    }
}

// --- NOWA FUNKCJA Z ANIMACJĄ ---
@Composable
fun LoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = "Loading",
        tint = Color.White, // Dodano kolor, aby ikona była widoczna na ciemnym tle
        modifier = Modifier
            .size(32.dp) // Zmniejszono rozmiar, aby pasował do interfejsu
            .graphicsLayer { rotationZ = rotation }
    )
}


@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: String?, // Dodaj parametr wybranej daty
    onDateClick: (String) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDay = currentMonth.atDay(1)
    val startDayOfWeek = firstDay.dayOfWeek.value % 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Nagłówki dni
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Nd", "Pn", "Wt", "Śr", "Cz", "Pt", "Sb").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dni miesiąca w wierszach (tygodniach)
        var dayCounter = 1
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break

            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    if ((week == 0 && dayOfWeek < startDayOfWeek) || dayCounter > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val date = currentMonth.atDay(dayCounter)
                        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val isToday = date == LocalDate.now()
                        val isSelected = dateString == selectedDate // Sprawdź czy dzień jest zaznaczony

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { onDateClick(dateString) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        isSelected -> Color(0xFFFF9800) // Pomarańczowy dla zaznaczonego
                                        isToday -> Color(0xFF4CAF50) // Zielony dla dzisiejszego
                                        else -> colorResource(id = R.color.moj_niebieski) // Niebieski dla reszty
                                    }
                                ),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text(
                                    text = dayCounter.toString(),
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}