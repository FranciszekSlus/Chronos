// Plik: ui/components/TaskFilterSheet.kt
package com.tasker.chronos.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasker.chronos.data.models.*
import com.tasker.chronos.ui.theme.BluePrimary
import com.tasker.chronos.ui.theme.blueWaveGradient
import com.tasker.chronos.ui.theme.textWithBlueGlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilterSheet(
    currentFilters: TaskFilters,
    onFiltersChanged: (TaskFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var filters by remember { mutableStateOf(currentFilters) }
    var showDatePicker by remember { mutableStateOf<DatePickerType?>(null) }

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
            // Header z gradientem
            // Header
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(blueWaveGradient())
                    .padding(top = 40.dp, bottom = 20.dp)
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔍 Filtruj Zadania",
                        style = MaterialTheme.typography.headlineSmall.merge(textWithBlueGlow()),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f) // ✅ DODAJ TO - żeby nie nakładał się na przycisk
                    )

                    IconButton(
                        onClick = {
                            filters = TaskFilters()
                            onFiltersChanged(filters)
                        }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Wyczyść wszystkie filtry",
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

            // Priorytety
            PrioritySection(
                selectedPriorities = filters.priorities,
                onPrioritiesChanged = { priorities ->
                    filters = filters.copy(priorities = priorities)
                }
            )

            // Zakres dat
            DateRangeSection(
                dateRange = filters.dateRange,
                onDateRangeChanged = { range ->
                    filters = filters.copy(dateRange = range)
                },
                onShowDatePicker = { type ->
                    showDatePicker = type
                }
            )

            // Sortowanie
            SortSection(
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

    // Date Picker Dialog
    showDatePicker?.let { type ->
        DatePickerDialog(
            type = type,
            currentDate = when (type) {
                DatePickerType.START -> filters.dateRange?.startDate
                DatePickerType.END -> filters.dateRange?.endDate
            },
            onDateSelected = { date ->
                val newRange = when (type) {
                    DatePickerType.START -> filters.dateRange?.copy(startDate = date)
                        ?: DateRange(startDate = date, endDate = null)
                    DatePickerType.END -> filters.dateRange?.copy(endDate = date)
                        ?: DateRange(startDate = null, endDate = date)
                }
                filters = filters.copy(dateRange = newRange)
                showDatePicker = null
            },
            onDismiss = { showDatePicker = null }
        )
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
            placeholder = { Text("Nazwa zadania...") },
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
private fun PrioritySection(
    selectedPriorities: Set<TaskPriority>,
    onPrioritiesChanged: (Set<TaskPriority>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Priorytet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskPriority.values().forEach { priority ->
                val isSelected = priority in selectedPriorities

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSet = if (isSelected) {
                            selectedPriorities - priority
                        } else {
                            selectedPriorities + priority
                        }
                        onPrioritiesChanged(newSet)
                    },
                    label = { Text(priority.toDisplayString()) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(priority.toColor())
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DateRangeSection(
    dateRange: DateRange?,
    onDateRangeChanged: (DateRange?) -> Unit,
    onShowDatePicker: (DatePickerType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zakres dat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = BluePrimary
            )

            if (dateRange != null) {
                TextButton(onClick = { onDateRangeChanged(null) }) {
                    Text("Wyczyść", color = Color.Red)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateButton(
                label = "Od",
                date = dateRange?.startDate,
                onClick = { onShowDatePicker(DatePickerType.START) },
                modifier = Modifier.weight(1f)
            )

            DateButton(
                label = "Do",
                date = dateRange?.endDate,
                onClick = { onShowDatePicker(DatePickerType.END) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DateButton(
    label: String,
    date: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = date?.let { formatDate(it) } ?: "Wybierz",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (date != null) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SortSection(
    sortBy: TaskSortOption,
    onSortChanged: (TaskSortOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Sortowanie",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TaskSortOption.values().forEach { option ->
                SortOptionItem(
                    option = option,
                    isSelected = sortBy == option,
                    onClick = { onSortChanged(option) }
                )
            }
        }
    }
}

@Composable
private fun SortOptionItem(
    option: TaskSortOption,
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

enum class DatePickerType {
    START, END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    type: DatePickerType,
    currentDate: String?,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate?.let {
            LocalDate.parse(it).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .toString()
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
