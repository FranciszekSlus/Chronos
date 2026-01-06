// Plik: ui/screens/TodayScheduleScreen.kt
package com.tasker.chronos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasker.chronos.ui.components.calendar.EventCard
import com.tasker.chronos.viewmodels.EventsViewModel

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScheduleScreen(
    eventsViewModel: EventsViewModel
) {
    val today = LocalDate.now()
    val events by eventsViewModel.allEvents.collectAsState()
    val todayEvents = events.filter { it.date == today.toString() }
        .sortedBy { it.startTime ?: "00:00" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dziś: ${today.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        if (todayEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📅", style = MaterialTheme.typography.displayLarge)
                    Text(
                        "Brak wydarzeń na dziś",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(todayEvents, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onClick = {
                            // TODO: Handle click - navigate to source
                        }
                    )
                }
            }
        }
    }
}
