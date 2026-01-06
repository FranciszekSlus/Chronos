// Plik: ui/components/GoalFilterSheet.kt
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
fun GoalFilterSheet(
    currentFilters: GoalFilters,
    onFiltersChanged: (GoalFilters) -> Unit,
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
                    .padding(horizontal = 20.dp, vertical = 20.dp) ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 Filtruj Cele",
                        style = MaterialTheme.typography.headlineSmall.merge(textWithBlueGlow()),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            filters = GoalFilters()
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
            GoalSearchSection(
                searchQuery = filters.searchQuery,
                onSearchChanged = { query ->
                    filters = filters.copy(searchQuery = query)
                }
            )

            // Zakres postępu
            ProgressRangeSection(
                progressRange = filters.progressRange,
                onProgressRangeChanged = { range ->
                    filters = filters.copy(progressRange = range)
                }
            )

            // Pokaż ukończone
            GoalShowCompletedSection(
                showCompleted = filters.showCompleted,
                onShowCompletedChanged = { show ->
                    filters = filters.copy(showCompleted = show)
                }
            )

            // Sortowanie
            GoalSortSection(
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
private fun GoalSearchSection(
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
            placeholder = { Text("Nazwa celu lub notatki...") },
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
private fun ProgressRangeSection(
    progressRange: ProgressRange?,
    onProgressRangeChanged: (ProgressRange?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zakres postępu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = BluePrimary
            )

            if (progressRange != null) {
                TextButton(onClick = { onProgressRangeChanged(null) }) {
                    Text("Wyczyść", color = Color.Red)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressRangeChip(
                    range = ProgressRange.RANGE_0_25,
                    isSelected = progressRange == ProgressRange.RANGE_0_25,
                    onClick = { onProgressRangeChanged(if (progressRange == ProgressRange.RANGE_0_25) null else ProgressRange.RANGE_0_25) }
                )
                ProgressRangeChip(
                    range = ProgressRange.RANGE_25_50,
                    isSelected = progressRange == ProgressRange.RANGE_25_50,
                    onClick = { onProgressRangeChanged(if (progressRange == ProgressRange.RANGE_25_50) null else ProgressRange.RANGE_25_50) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressRangeChip(
                    range = ProgressRange.RANGE_50_75,
                    isSelected = progressRange == ProgressRange.RANGE_50_75,
                    onClick = { onProgressRangeChanged(if (progressRange == ProgressRange.RANGE_50_75) null else ProgressRange.RANGE_50_75) }
                )
                ProgressRangeChip(
                    range = ProgressRange.RANGE_75_100,
                    isSelected = progressRange == ProgressRange.RANGE_75_100,
                    onClick = { onProgressRangeChanged(if (progressRange == ProgressRange.RANGE_75_100) null else ProgressRange.RANGE_75_100) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.ProgressRangeChip(
    range: ProgressRange,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(range.toDisplayString()) },
        modifier = Modifier.weight(1f)
    )
}

@Composable
private fun GoalShowCompletedSection(
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
                text = "Pokaż ukończone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = BluePrimary
            )
            Text(
                text = "Cele ze 100% postępem",
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
private fun GoalSortSection(
    sortBy: GoalSortOption,
    onSortChanged: (GoalSortOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Sortowanie",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            GoalSortOption.values().forEach { option ->
                GoalSortOptionItem(
                    option = option,
                    isSelected = sortBy == option,
                    onClick = { onSortChanged(option) }
                )
            }
        }
    }
}

@Composable
private fun GoalSortOptionItem(
    option: GoalSortOption,
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