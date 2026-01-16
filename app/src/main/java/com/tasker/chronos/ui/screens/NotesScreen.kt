package com.tasker.chronos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasker.chronos.data.models.FormatType
import com.tasker.chronos.data.models.Note
import com.tasker.chronos.data.models.NoteCategory
import com.tasker.chronos.data.models.TextFormat
import com.tasker.chronos.ui.components.GlassCard
import com.tasker.chronos.viewmodels.NotesViewModel
import java.time.format.DateTimeFormatter
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

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

    // Filtrowane notatki
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

    // ✅ JEŚLI JESTEŚMY W TRYBIE EDYCJI LUB TWORZENIA - POKAŻ PEŁNOEKRANOWY EDYTOR
    if (isCreatingNewNote) {
        FullScreenNoteEditor(
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
        FullScreenNoteEditor(
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
            onTogglePin = {
                notesViewModel.togglePin(note.id)
            }
        )
        return
    }

    // ✅ GŁÓWNY EKRAN LISTY NOTATEK
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📝 Notatki",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Category, "Kategorie")
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
                Icon(Icons.Default.Add, "Dodaj notatkę")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtry kategorii
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

            // Lista notatek
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

    // Dialog kategorii
    if (showAddCategoryDialog) {
        ManageCategoriesDialog(
            categories = categories,
            onDismiss = { showAddCategoryDialog = false },
            onAddCategory = { category ->
                notesViewModel.addCategory(category)
            },
            onDeleteCategory = { categoryId ->
                notesViewModel.deleteCategory(categoryId)
            }
        )
    }
}

@Composable
fun NoteItem(
    note: Note,
    category: NoteCategory?,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (note.isPinned) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Przypięta",
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content.take(100) + if (note.content.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (category != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(android.graphics.Color.parseColor(category.color)),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = note.updatedAt.format(DateTimeFormatter.ofPattern("dd.MM HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}


// ✅ NOWY EDYTOR Z PRAWDZIWYM FORMATOWANIEM
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenNoteEditor(
    note: Note?,
    categories: List<NoteCategory>,
    onSave: (Note) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onTogglePin: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var selectedCategoryId by remember { mutableStateOf(note?.categoryId) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showFormattingToolbar by remember { mutableStateOf(false) }

    // ✅ Rich Text State - obsługuje formatowanie
    val richTextState = rememberRichTextState()

    // ✅ Załaduj zawartość notatki (HTML)
    LaunchedEffect(note) {
        if (note != null && note.content.isNotBlank()) {
            richTextState.setHtml(note.content)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (note != null) {
                        Text(
                            note.updatedAt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showFormattingToolbar = !showFormattingToolbar }) {
                        Icon(
                            Icons.Default.FormatSize,
                            "Formatowanie",
                            tint = if (showFormattingToolbar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (note != null && onTogglePin != null) {
                        IconButton(onClick = onTogglePin) {
                            Icon(
                                Icons.Default.PushPin,
                                "Przypnij",
                                tint = if (note.isPinned) Color(0xFFFF6B35) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(onClick = { showCategoryPicker = true }) {
                        Icon(Icons.Default.Category, "Kategoria")
                    }

                    if (onDelete != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Usuń",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                val htmlContent = richTextState.toHtml() // ✅ Zapisz jako HTML
                                android.util.Log.d("NoteEditor", "Zapisuję: title=$title, HTML length=${htmlContent.length}")

                                onSave(
                                    note?.copy(
                                        title = title,
                                        content = htmlContent,
                                        categoryId = selectedCategoryId
                                    ) ?: Note(
                                        title = title,
                                        content = htmlContent,
                                        categoryId = selectedCategoryId
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, "Zapisz")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ✅ Toolbar formatowania
            AnimatedVisibility(visible = showFormattingToolbar) {
                RichTextFormattingToolbar(
                    state = richTextState,
                    onInsertCheckbox = {
                        richTextState.toggleUnorderedList()
                    }
                )
            }

            // Kategoria
            if (selectedCategoryId != null) {
                val category = categories.find { it.id == selectedCategoryId }
                if (category != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.15f),
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 12.dp, bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(category.color)))
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(android.graphics.Color.parseColor(category.color)),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Tytuł
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "Tytuł",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = false,
                maxLines = 3
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            // ✅ RICH TEXT EDITOR - PRAWDZIWE FORMATOWANIE!
            RichTextEditor(
                state = richTextState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp
                ),
                placeholder = {
                    Text(
                        "Zacznij pisać...",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                },
                colors = RichTextEditorDefaults.richTextEditorColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }

    // Dialogi (bez zmian)
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Wybierz kategorię") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategoryId = null
                                showCategoryPicker = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedCategoryId == null)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "Brak kategorii",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    categories.forEach { category ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategoryId = category.id
                                    showCategoryPicker = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedCategoryId == category.id)
                                Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(category.color)))
                                )
                                Text(
                                    category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selectedCategoryId == category.id)
                                        FontWeight.Bold
                                    else
                                        FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Zamknij")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Usuń notatkę?") },
            text = { Text("Czy na pewno chcesz usunąć tę notatkę? Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete?.invoke() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

// ✅ NOWY TOOLBAR DLA RICH TEXT EDITOR
@Composable
fun RichTextFormattingToolbar(
    state: com.mohamedrejeb.richeditor.model.RichTextState,
    onInsertCheckbox: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val defaultColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ✅ Pogrubienie
            item {
                IconButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                        android.util.Log.d("RichEditor", "Bold toggled")
                    }
                ) {
                    Icon(
                        Icons.Default.FormatBold,
                        "Pogrubienie",
                        modifier = Modifier.size(20.dp),
                        tint = if (state.currentSpanStyle.fontWeight == FontWeight.Bold) primaryColor else defaultColor
                    )
                }
            }

            // ✅ Kursywa
            item {
                IconButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ))
                    }
                ) {
                    Icon(
                        Icons.Default.FormatItalic,
                        "Kursywa",
                        modifier = Modifier.size(20.dp),
                        tint = if (state.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic)
                            primaryColor else defaultColor
                    )
                }
            }

            // ✅ Podkreślenie
            item {
                IconButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ))
                    }
                ) {
                    Icon(
                        Icons.Default.FormatUnderlined,
                        "Podkreślenie",
                        modifier = Modifier.size(20.dp),
                        tint = if (state.currentSpanStyle.textDecoration == androidx.compose.ui.text.style.TextDecoration.Underline)
                            primaryColor else defaultColor
                    )
                }
            }

            // ✅ Przekreślenie
            item {
                IconButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        ))
                    }
                ) {
                    Icon(
                        Icons.Default.FormatStrikethrough,
                        "Przekreślenie",
                        modifier = Modifier.size(20.dp),
                        tint = if (state.currentSpanStyle.textDecoration == androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            primaryColor else defaultColor
                    )
                }
            }

            item {
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }

            // ✅ Nagłówek 1
            item {
                TextButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold))
                    }
                ) {
                    Text(
                        "H1",
                        fontWeight = FontWeight.Bold,
                        color = if (state.currentSpanStyle.fontSize == 28.sp) primaryColor else defaultColor
                    )
                }
            }

            // ✅ Nagłówek 2
            item {
                TextButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                    }
                ) {
                    Text(
                        "H2",
                        fontWeight = FontWeight.Bold,
                        color = if (state.currentSpanStyle.fontSize == 22.sp) primaryColor else defaultColor
                    )
                }
            }

            item {
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }

            // ✅ Lista nieuporządkowana (checkbox)
            item {
                IconButton(onClick = onInsertCheckbox) {
                    Icon(Icons.Default.CheckBox, "Lista", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ✅ NOWY KOMPONENT - Renderowanie treści z formatowaniem
@Composable
fun FormattedTextContent(
    content: String,
    formatting: List<TextFormat>,
    onContentChange: (String) -> Unit,
    onFormattingChange: (List<TextFormat>) -> Unit,
    textFieldValueState: androidx.compose.ui.text.input.TextFieldValue,
    onTextFieldValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Podziel tekst na linie dla obsługi checkboxów
        val lines = content.split("\n")
        var currentIndex = 0

        lines.forEachIndexed { lineIndex, line ->
            val lineStart = currentIndex
            val lineEnd = currentIndex + line.length

            // Sprawdź czy linia zawiera checkbox
            val checkboxFormat = formatting.find {
                it.start >= lineStart && it.start < lineEnd &&
                        (it.type == FormatType.CHECKBOX_UNCHECKED || it.type == FormatType.CHECKBOX_CHECKED)
            }

            if (checkboxFormat != null && line.startsWith("☐") || line.startsWith("☑")) {
                // Renderuj jako checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = line.startsWith("☑"),
                        onCheckedChange = { checked ->
                            val newChar = if (checked) "☑" else "☐"
                            val newLine = newChar + line.substring(1)
                            val newContent = lines.toMutableList().apply {
                                this[lineIndex] = newLine
                            }.joinToString("\n")

                            onContentChange(newContent)

                            // Zaktualizuj formatowanie
                            val newFormatting = formatting.map { format ->
                                if (format.start == checkboxFormat?.start) {
                                    format.copy(
                                        type = if (checked) FormatType.CHECKBOX_CHECKED else FormatType.CHECKBOX_UNCHECKED
                                    )
                                } else format
                            }
                            onFormattingChange(newFormatting)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = line.substring(2).trim(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (line.startsWith("☑"))
                                androidx.compose.ui.text.style.TextDecoration.LineThrough
                            else null
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            currentIndex = lineEnd + 1 // +1 dla \n
        }

        // TextField dla edycji (niewidoczny gdy są checkboxy, albo zawsze widoczny)
        TextField(
            value = textFieldValueState,
            onValueChange = onTextFieldValueChange,
            placeholder = {
                Text(
                    "Zacznij pisać...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}



// ✅ TEXTFIELD Z FORMATOWANIEM
@Composable
fun FormattedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    formatting: List<TextFormat>,
    onSelectionChange: (Int, Int) -> Unit,
    onCheckboxToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textFieldValue = remember(value) {
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(value))
    }

    // Aplikuj formatowanie
    val annotatedString = buildAnnotatedString {
        append(value)

        formatting.forEach { format ->
            when (format.type) {
                FormatType.BOLD -> addStyle(
                    androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold),
                    format.start,
                    format.end
                )
                FormatType.ITALIC -> addStyle(
                    androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    format.start,
                    format.end
                )
                FormatType.UNDERLINE -> addStyle(
                    androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
                    format.start,
                    format.end
                )
                FormatType.STRIKETHROUGH -> addStyle(
                    androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                    format.start,
                    format.end
                )
                FormatType.HEADING1 -> addStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    format.start,
                    format.end
                )
                FormatType.HEADING2 -> addStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    format.start,
                    format.end
                )
                else -> {} // CHECKBOX obsługiwany osobno
            }
        }
    }

    Column(modifier = modifier) {
        BasicTextField(
            value = textFieldValue.value.copy(annotatedString = annotatedString),
            onValueChange = { newValue ->
                textFieldValue.value = newValue
                onValueChange(newValue.text)
                onSelectionChange(newValue.selection.start, newValue.selection.end)
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            ),
            modifier = Modifier.fillMaxSize(),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            "Zacznij pisać...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
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
        title = { Text("Zarządzaj kategoriami") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dodaj nową kategorię
                Text("Dodaj kategorię:", style = MaterialTheme.typography.labelMedium)
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
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.padding(4.dp)
                                    } else Modifier
                                ),
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

                Divider()

                // Lista istniejących kategorii
                Text("Istniejące kategorie:", style = MaterialTheme.typography.labelMedium)

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
                                    contentDescription = "Usuń",
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
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}