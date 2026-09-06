package com.tasker.chronos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasker.chronos.data.models.Note
import com.tasker.chronos.data.models.NoteCategory
import com.tasker.chronos.viewmodels.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notesViewModel: NotesViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val notes by notesViewModel.notes.collectAsState()
    val categories by notesViewModel.categories.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var isCreatingNewNote by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, selectedCategoryFilter) {
        if (selectedCategoryFilter != null) {
            notes.filter { it.categoryId == selectedCategoryFilter }
        } else {
            notes
        }.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.updatedAt }
        )
    }

    if (isCreatingNewNote) {
        NoteEditorScreen(
            note = null,
            categories = categories,
            onSave = { newNote ->
                notesViewModel.addNote(newNote)
                isCreatingNewNote = false
            },
            onCancel = { isCreatingNewNote = false }
        )
        return
    }

    selectedNote?.let { note ->
        NoteEditorScreen(
            note = note,
            categories = categories,
            onSave = { updatedNote ->
                notesViewModel.updateNote(updatedNote)
                selectedNote = null
            },
            onCancel = { selectedNote = null },
            onDelete = {
                notesViewModel.deleteNote(note.id)
                selectedNote = null
            },
            onTogglePin = { notesViewModel.togglePin(note.id) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notatki",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wroc")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Category, contentDescription = "Kategorie")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNewNote = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj notatke")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("Wszystkie (${notes.size})") }
                        )
                    }
                    items(categories, key = { it.id }) { category ->
                        val count = notes.count { it.categoryId == category.id }
                        FilterChip(
                            selected = selectedCategoryFilter == category.id,
                            onClick = { selectedCategoryFilter = category.id },
                            label = { Text("${category.name} ($count)") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(category.color)))
                                )
                            }
                        )
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.StickyNote2,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "Brak notatek",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteItem(
                            note = note,
                            category = categories.find { it.id == note.categoryId },
                            onClick = { selectedNote = note }
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        ManageCategoriesDialog(
            categories = categories,
            onDismiss = { showAddCategoryDialog = false },
            onAddCategory = { category -> notesViewModel.addCategory(category) },
            onDeleteCategory = { categoryId -> notesViewModel.deleteCategory(categoryId) }
        )
    }
}

@Composable
fun ManageCategoriesDialog(
    categories: List<NoteCategory>,
    onDismiss: () -> Unit,
    onAddCategory: (NoteCategory) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#2196F3") }

    val availableColors = listOf(
        "#FF6B35", "#2196F3", "#4CAF50", "#FFD700",
        "#9C27B0", "#FF5722", "#00BCD4", "#FFC107"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zarzadzaj kategoriami") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dodaj kategorie:", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nazwa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCategory(NoteCategory(name = newCategoryName, color = selectedColor))
                            newCategoryName = ""
                        }
                    },
                    enabled = newCategoryName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj")
                }

                HorizontalDivider()

                Text("Istniejace kategorie:", style = MaterialTheme.typography.labelMedium)

                categories.forEach { category ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(category.color)))
                                )
                                Text(category.name, style = MaterialTheme.typography.bodyMedium)
                            }

                            IconButton(
                                onClick = { onDeleteCategory(category.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Usun",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zamknij") }
        }
    )
}
