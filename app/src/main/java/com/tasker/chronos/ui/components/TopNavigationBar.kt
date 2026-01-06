package com.tasker.chronos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tasker.chronos.R

// Enum do reprezentowania aktywnego ekranu
enum class Screen {
    HABITS, TASKS, GOALS
}

@Composable
fun TopNavigationBar(
    navController: NavController,
    activeScreen: Screen,
    // Dodajemy nowy parametr na tytuł
    screenTitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // Dodajemy padding, aby strzałki nie były przyklejone do krawędzi
        horizontalArrangement = Arrangement.SpaceBetween, // Zmieniamy na SpaceBetween
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Strzałka w lewo: widoczna tylko dla Zadań i Celów
        if (activeScreen == Screen.TASKS || activeScreen == Screen.GOALS) {
            IconButton(onClick = {
                when (activeScreen) {
                    Screen.TASKS -> navController.navigate("start") { popUpTo("start") { inclusive = true } }
                    Screen.GOALS -> navController.navigate("zadania") { popUpTo("zadania") { inclusive = true } }
                    else -> {}
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Poprzedni ekran", tint = Color.White)
            }
        } else {
            // Pusty Spacer, aby zachować symetrię układu
            Spacer(modifier = Modifier.width(48.dp)) // Szerokość IconButton
        }

        // --- POCZĄTEK POPRAWKI ---
        // Usunięto przyciski, w ich miejsce wstawiono tytuł ekranu
        Text(
            text = screenTitle,
            color = Color.White,
            fontSize = 28.sp,
            fontFamily = FontFamily(Font(R.font.blackops)),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f) // Tytuł zajmuje całą dostępną przestrzeń na środku
        )
        // --- KONIEC POPRAWKI ---

        // Strzałka w prawo: widoczna tylko dla Nawyków i Zadań
        if (activeScreen == Screen.HABITS || activeScreen == Screen.TASKS) {
            IconButton(onClick = {
                when (activeScreen) {
                    Screen.HABITS -> navController.navigate("zadania")
                    Screen.TASKS -> navController.navigate("cele")
                    else -> {}
                }
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Następny ekran", tint = Color.White)
            }
        } else {
            // Pusty Spacer, aby zachować symetrię układu
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}