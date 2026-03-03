// Plik: ui/screens/SettingsScreen.kt
package com.tasker.chronos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.viewmodels.UserProfileViewModel
import com.tasker.chronos.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfileViewModel: UserProfileViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToArchive: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val userProfile by userProfileViewModel.userProfile.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Sekcja: Profil
            SectionHeader("Profil")

            ProfileCard(
                totalPoints = userProfile.totalPoints,
                onResetPoints = { showResetDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sekcja: Powiadomienia
            SectionHeader("Powiadomienia")

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Powiadomienia",
                subtitle = if (settings.notificationsEnabled) "Włączone" else "Wyłączone",
                trailing = {
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { settingsViewModel.setNotificationsEnabled(it) }
                    )
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Sekcja: Archiwum
            SectionHeader("Dane")

            SettingsItem(
                icon = Icons.Default.Archive,
                title = "Archiwum",
                subtitle = "Przeglądaj ukończone elementy",
                onClick = onNavigateToArchive
            )

            Spacer(modifier = Modifier.height(24.dp))

// Sekcja: Statystyki
            SectionHeader("Statystyki")

            SettingsItem(
                icon = Icons.Default.BarChart, // ✅ DODAJ
                title = "Statystyki",
                subtitle = "Zobacz swoją aktywność",
                onClick = onNavigateToStatistics
            )

            Spacer(modifier = Modifier.height(24.dp))



            Spacer(modifier = Modifier.height(24.dp))

            // Sekcja: Pomoc
            SectionHeader("Pomoc")

            SettingsItem(
                icon = Icons.Default.Help,
                title = "Tutorial",
                subtitle = "Zobacz przewodnik po aplikacji",
                onClick = onNavigateToHelp
            )

            SettingsItem(
                icon = Icons.Default.Info,
                title = "O aplikacji",
                subtitle = "Chronos 0.2 v @elozelocompany 01.02.2026 (ostatnia modyfikacja 03.03.2026)"

            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialog resetowania punktów
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset punktów") },
            text = { Text("Czy na pewno chcesz zresetować wszystkie punkty? Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userProfileViewModel.resetPoints()
                        showResetDialog = false
                    }
                ) {
                    Text("Resetuj", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Dialog eksportu danych
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Eksport danych") },
            text = { Text("Funkcja eksportu danych zostanie wkrótce dodana.") },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ProfileCard(
    totalPoints: Int,
    onResetPoints: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$totalPoints",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Łączna liczba punktów",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onResetPoints,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset punktów")
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Divider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}