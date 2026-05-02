// Plik: MainActivity.kt
package com.tasker.chronos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.tasker.chronos.navigation.NotificationNavigationTarget
import com.tasker.chronos.navigation.ChronosNavigation
import com.tasker.chronos.ui.screens.LoadingScreen
import com.tasker.chronos.ui.theme.ChronosTheme
import com.tasker.chronos.workers.HabitResetWorker

class MainActivity : ComponentActivity() {
    private val notificationTargetState = mutableStateOf<NotificationNavigationTarget?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "✅ Uprawnienie do powiadomień przyznane")
        } else {
            android.util.Log.w("MainActivity", "⚠️ Brak uprawnień do powiadomień")
        }
    }

    // MainActivity.kt - ZNAJDŹ TĘ SEKCJĘ:

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("MainActivity", "🚀 MainActivity.onCreate()")

        checkNotificationPermission()
        initializeNotifications()
        notificationTargetState.value = parseNotificationIntent(intent)

        setContent {
            ChronosTheme {
                // ✅ NOWE: Splash screen z prawdziwym ładowaniem
                var isAppReady by remember { mutableStateOf(false) }

                // ✅ Sprawdź czy aplikacja jest gotowa
                LaunchedEffect(Unit) {
                    // Tutaj możesz dodać rzeczywiste ładowanie danych
                    // np. inicjalizacja ViewModeli, ładowanie z plików, itp.
                    kotlinx.coroutines.delay(1500) // Minimum 1.5s dla płynności animacji
                    isAppReady = true
                }

                if (isAppReady) {
                    // ✅ Aplikacja gotowa - pokaż nawigację
                    ChronosNavigation(notificationTarget = notificationTargetState.value)
                } else {
                    // ✅ Ładowanie - pokaż splash screen
                    LoadingScreen()
                }
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
        setIntent(intent)
        notificationTargetState.value = parseNotificationIntent(intent)
    }

    private fun parseNotificationIntent(intent: android.content.Intent?): NotificationNavigationTarget? {
        if (intent == null) return null

        val taskId = intent.getStringExtra("open_task")
            ?: intent.takeIf { it.getStringExtra("item_type") == "task" }?.getStringExtra("item_id")
        if (!taskId.isNullOrBlank()) {
            return NotificationNavigationTarget(
                tabIndex = 0,
                taskId = taskId
            )
        }

        val habitId = intent.getStringExtra("open_habit")
            ?: intent.takeIf { it.getStringExtra("item_type") == "habit" }?.getStringExtra("item_id")
        if (!habitId.isNullOrBlank()) {
            return NotificationNavigationTarget(
                tabIndex = 1,
                habitId = habitId
            )
        }

        val goalId = intent.getStringExtra("open_goal")
            ?: intent.takeIf { it.getStringExtra("item_type") == "goal" }?.getStringExtra("item_id")
        if (!goalId.isNullOrBlank()) {
            return NotificationNavigationTarget(
                tabIndex = 2,
                goalId = goalId
            )
        }

        val openCalendar = intent.getBooleanExtra("open_calendar", false) ||
            intent.getStringExtra("item_type") == "event"
        if (openCalendar) {
            val rawEventId = intent.getStringExtra("open_event_id")
                ?: intent.getStringExtra("event_id")
            val calendarEventId = rawEventId?.removeSuffix("_reminder")?.takeIf { it.isNotBlank() }
            val calendarEventDate = intent.getStringExtra("open_event_date")
                ?: intent.getStringExtra("event_date")
            return NotificationNavigationTarget(
                tabIndex = 0,
                openCalendar = true,
                calendarEventId = calendarEventId,
                calendarEventDate = calendarEventDate
            )
        }

        return null
    }
}