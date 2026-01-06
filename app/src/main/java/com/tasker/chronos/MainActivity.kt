// Plik: MainActivity.kt
package com.tasker.chronos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tasker.chronos.navigation.ChronosNavigation
import com.tasker.chronos.ui.screens.StartScreen
import com.tasker.chronos.ui.screens.CalendarScreen
import com.tasker.chronos.ui.screens.SettingsScreen
import com.tasker.chronos.ui.theme.ChronosTheme
import com.tasker.chronos.workers.HabitResetWorker

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "✅ Uprawnienie do powiadomień przyznane")
        } else {
            android.util.Log.w("MainActivity", "⚠️ Brak uprawnień do powiadomień")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("MainActivity", "🚀 MainActivity.onCreate()")

        checkNotificationPermission()
        initializeNotifications()

        setContent {
            ChronosTheme {
                // ✅ Używamy Twojej gotowej nawigacji
                ChronosNavigation()
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    android.util.Log.d("MainActivity", "✅ Uprawnienia OK")
                }
                else -> {
                    android.util.Log.d("MainActivity", "📩 Proszę o uprawnienia...")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun initializeNotifications() {
        try {
            HabitResetWorker.scheduleMidnightReset(this)
            android.util.Log.d("MainActivity", "✅ Powiadomienia zainicjalizowane")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "❌ Błąd inicjalizacji: ${e.message}", e)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)

        val itemId = intent.getStringExtra("item_id")
        val itemType = intent.getStringExtra("item_type")

        when (itemType) {
            "habit" -> android.util.Log.d("MainActivity", "🔔 Otwórz nawyk: $itemId")
            "task" -> android.util.Log.d("MainActivity", "🔔 Otwórz zadanie: $itemId")
            "goal" -> android.util.Log.d("MainActivity", "🔔 Otwórz cel: $itemId")
        }
    }
}

// ═══════════════════════════════════════════════════════════
// GŁÓWNY EKRAN Z NAWIGACJĄ
// ═══════════════════════════════════════════════════════════



@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("home", "Start", Icons.Default.Home),
        BottomNavItem("calendar", "Kalendarz", Icons.Default.CalendarToday),
        BottomNavItem("settings", "Ustawienia", Icons.Default.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)