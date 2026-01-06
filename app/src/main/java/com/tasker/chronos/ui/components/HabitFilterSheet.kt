// Plik: ui/components/HabitFilterSheet.kt
package com.tasker.chronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasker.chronos.data.models.*
import com.tasker.chronos.ui.theme.BluePrimary
import com.tasker.chronos.ui.theme.blueWaveGradient
import com.tasker.chronos.ui.theme.textWithBlueGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFilterSheet(
    currentFilters: HabitFilters,
    onFiltersChanged: (HabitFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var filters by remember { mutableStateOf(currentFilters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp), // ✅ Bez zaokrągleń
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()), // ✅ Scroll bez paddingu
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            // Header
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(blueWaveGradient())
                    .padding(top = 40.dp)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 Filtruj Nawyki",
                        style = MaterialTheme.typography.headlineSmall.merge(textWithBlueGlow()),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            filters = HabitFilters()
                            onFiltersChanged(filters)
                        }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Wyczyść filtry",
                            tint = Color.White
                        )
                    }
                }
            }

            // Wyszukiwarka
            SearchSection(
                searchQuery = filters.searchQuery,
                onSearchChanged = { query ->
                    filters = filters.copy(searchQuery = query)
                }
            )

            // Częstotliwość
            FrequencySection(
                selectedFrequencies = filters.frequencies,
                onFrequenciesChanged = { frequencies ->
                    filters = filters.copy(frequencies = frequencies)
                }
            )

            // Pokaż ukończone
            ShowCompletedSection(
                showCompleted = filters.showCompleted,
                onShowCompletedChanged = { show ->
                    filters = filters.copy(showCompleted = show)
                }
            )

            // Minimalny streak
            MinStreakSection(
                minStreak = filters.minStreak,
                onMinStreakChanged = { streak ->
                    filters = filters.copy(minStreak = streak)
                }
            )

            // Sortowanie
            HabitSortSection(
                sortBy = filters.sortBy,
                onSortChanged = { sort ->
                    filters = filters.copy(sortBy = sort)
                }
            )

            // Przyciski akcji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anuluj")
                }

                Button(
                    onClick = {
                        onFiltersChanged(filters)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary
                    )
                ) {
                    Text("Zastosuj")
                }
            }
        }
    }
}

@Composable
private fun SearchSection(
    searchQuery: String,
    onSearchChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Wyszukaj",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = { Text("Nazwa nawyku...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

@Composable
private fun FrequencySection(
    selectedFrequencies: Set<HabitFrequency>,
    onFrequenciesChanged: (Set<HabitFrequency>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Częstotliwość",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HabitFrequency.values().forEach { frequency ->
                val isSelected = frequency in selectedFrequencies

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSet = if (isSelected) {
                            selectedFrequencies - frequency
                        } else {
                            selectedFrequencies + frequency
                        }
                        onFrequenciesChanged(newSet)
                    },
                    label = { Text(frequency.toDisplayString()) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ShowCompletedSection(
    showCompleted: Boolean,
    onShowCompletedChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pokaż ukończone dzisiaj",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = BluePrimary
            )
            Text(
                text = "Nawyki z checkmarkiem dzisiaj",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = showCompleted,
            onCheckedChange = onShowCompletedChanged
        )
    }
}

@Composable
private fun MinStreakSection(
    minStreak: Int?,
    onMinStreakChanged: (Int?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Minimalny streak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = BluePrimary
            )

            if (minStreak != null) {
                TextButton(onClick = { onMinStreakChanged(null) }) {
                    Text("Wyczyść", color = Color.Red)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(3, 7, 14, 30).forEach { streak ->
                val isSelected = minStreak == streak

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onMinStreakChanged(if (isSelected) null else streak)
                    },
                    label = { Text("${streak}+ dni") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HabitSortSection(
    sortBy: HabitSortOption,
    onSortChanged: (HabitSortOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Sortowanie",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            HabitSortOption.values().forEach { option ->
                HabitSortOptionItem(
                    option = option,
                    isSelected = sortBy == option,
                    onClick = { onSortChanged(option) }
                )
            }
        }
    }
}

@Composable
private fun HabitSortOptionItem(
    option: HabitSortOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected)
            BluePrimary.copy(alpha = 0.1f)
        else
            Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.toDisplayString(),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onSurface
            )

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = BluePrimary
                )
            }
        }
    }
}