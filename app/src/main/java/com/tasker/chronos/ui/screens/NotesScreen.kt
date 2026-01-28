package com.tasker.chronos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.tasker.chronos.data.models.Note
import com.tasker.chronos.data.models.NoteCategory
import com.tasker.chronos.ui.components.GlassCard
import com.tasker.chronos.viewmodels.NotesViewModel
import java.time.format.DateTimeFormatter
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.room.util.copy
import java.time.LocalDateTime
import java.util.UUID
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.tasker.chronos.ui.components.CHECKBOX_CHECKED
import com.tasker.chronos.ui.components.CHECKBOX_UNCHECKED
import com.tasker.chronos.ui.components.parseCheckboxes
import com.tasker.chronos.ui.components.toggleCheckboxAtPosition


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
//    var checkboxStates by remember { mutableStateOf(note?.checkboxItems ?: emptyMap()) }
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
    // ✅ State do renderowania HTML
    val richTextState = rememberRichTextState()

    LaunchedEffect(note.content) {
        if (note.content.isNotBlank()) {
            richTextState.setHtml(note.content)
        }
    }

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

                // ✅ Renderuj HTML jako tekst (bez tagów)
                val plainText = richTextState.annotatedString.text
                Text(
                    text = plainText.take(100) + if (plainText.length > 100) "..." else "",
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
// ✅ PEŁNY EDYTOR Z POPRAWIONYMI CHECKBOXAMI I STYLAMI LIST
// ✅ EDYTOR Z PRAWDZIWYMI KLIKALNYMI CHECKBOXAMI
// ✅ NOWY PROSTY EDYTOR - CHECKBOXY W TEKŚCIE
// ✅ KOMPLETNY EDYTOR - FORMATOWANIE + KLIKALNE CHECKBOXY
// ✅ HYBRYDOWY EDYTOR - FORMATOWANIE + KLIKALNE CHECKBOXY
// ✅ PROSTY EDYTOR - CHECKBOX JAKO ZWYKŁY ZNAK
// ✅ DZIAŁAJĄCA WERSJA - RICH TEXT EDITOR Z FORMATOWANIEM
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

    val richTextState = rememberRichTextState()

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
                                val htmlContent = richTextState.toHtml()
                                android.util.Log.d("NoteEditor", "Zapisuję: HTML length=${htmlContent.length}")

                                onSave(
                                    note?.copy(
                                        title = title,
                                        content = richTextState.toHtml(),
                                        categoryId = selectedCategoryId,
                                        //checkboxItems = checkboxStates // ✅ DODAJ
                                    ) ?: Note(
                                        title = title,
                                        content = richTextState.toHtml(),
                                        categoryId = selectedCategoryId,
                                        //checkboxItems = checkboxStates // ✅ DODAJ
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
            // ✅ TOOLBAR Z FORMATOWANIEM
            AnimatedVisibility(visible = showFormattingToolbar) {
                FormattingToolbar(
                    state = richTextState,
                    onInsertCheckbox = { checkboxId ->
                        //checkboxStates = checkboxStates + (checkboxId to false)
                        android.util.Log.d("NoteEditor", "Checkbox dodany: $checkboxId")
                    }
                )
            }

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

            // ✅ RICH TEXT EDITOR
            // ✅ BOX Z EDYTOREM I CHECKBOXAMI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                // Rich Text Editor
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier.fillMaxSize(),
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

                // ✅ WARSTWA Z CHECKBOXAMI (overlay)
//                CheckboxOverlay(
//                    html = richTextState.toHtml(),
//                    checkboxStates = checkboxStates,
//                    onCheckboxToggle = { checkboxId ->
//                        checkboxStates = checkboxStates.mapValues { (id, checked) ->
//                            if (id == checkboxId) !checked else checked
//                        }
//
//                        // Zaktualizuj HTML - zamień ☐ na ☑ lub odwrotnie
//                        val currentHtml = richTextState.toHtml()
//                        val isChecked = checkboxStates[checkboxId] ?: false
//                        val newSymbol = if (isChecked) "☑" else "☐"
//                        val oldSymbol = if (isChecked) "☐" else "☑"
//
//                        val newHtml = currentHtml.replace(
//                            "data-checkbox='$checkboxId'>$oldSymbol",
//                            "data-checkbox='$checkboxId'>$newSymbol"
//                        )
//                        richTextState.setHtml(newHtml)
//
//                        android.util.Log.d(
//                            "NoteEditor",
//                            "Checkbox $checkboxId przełączony: $isChecked"
//                        )
//                    }
//                )
            }
        }
    }

    // Dialogi
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

// ✅ TOOLBAR Z WSZYSTKIMI FUNKCJAMI
@Composable
fun FormattingToolbar(
    state: com.mohamedrejeb.richeditor.model.RichTextState,
    onInsertCheckbox: (String) -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val defaultColor = MaterialTheme.colorScheme.onSurfaceVariant

    var showListMenu by remember { mutableStateOf(false) }

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
            // Bold
            item {
                IconButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
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

            // Italic
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

            // Underline
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

            // Strikethrough
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

            // H1
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

            item {
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }

            // ✅ LISTY - MENU
            item {
                Box {
                    IconButton(onClick = { showListMenu = !showListMenu }) {
                        Icon(
                            Icons.Default.FormatListNumbered,
                            "Listy",
                            modifier = Modifier.size(20.dp),
                            tint = if (state.isOrderedList || state.isUnorderedList) primaryColor else defaultColor
                        )
                    }

                    DropdownMenu(
                        expanded = showListMenu,
                        onDismissRequest = { showListMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("1. Liczby (1, 2, 3...)") },
                            onClick = {
                                if (!state.isOrderedList) {
                                    state.toggleOrderedList()
                                }
                                showListMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("● Kropki") },
                            onClick = {
                                if (!state.isUnorderedList) {
                                    state.toggleUnorderedList()
                                }
                                showListMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ✅ PROSTY KLIKALNY TEXTFIELD
@Composable
fun ClickableTextField(
    text: String,
    onTextChange: (String) -> Unit,
    onCheckboxClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textFieldValue = remember(text) {
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(text))
    }

    Box(modifier = modifier) {
        TextField(
            value = textFieldValue.value,
            onValueChange = { newValue ->
                textFieldValue.value = newValue
                onTextChange(newValue.text)
            },
            placeholder = {
                Text(
                    "Zacznij pisać...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
            },
            modifier = Modifier.fillMaxSize(),
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

        // ✅ Warstwa klikalna nad TextFieldem - wykrywa kliknięcia w ☐/☑
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // Bez ripple
                ) {
                    // Wykryj pozycję kliknięcia i sprawdź czy to checkbox
                    val cursorPos = textFieldValue.value.selection.start
                    if (cursorPos < text.length &&
                        (text[cursorPos] == CHECKBOX_UNCHECKED[0] || text[cursorPos] == CHECKBOX_CHECKED[0])) {
                        onCheckboxClick(cursorPos)
                    }
                }
        )
    }
}

// ✅ PROSTY TOOLBAR
@Composable
fun SimpleToolbar(
    onInsertCheckbox: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onInsertCheckbox) {
                Icon(
                    Icons.Default.CheckBox,
                    "Dodaj zadanie",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ✅ KOMPLETNY TOOLBAR - WSZYSTKIE FUNKCJE
@Composable
fun CompleteFormattingToolbar(
    state: com.mohamedrejeb.richeditor.model.RichTextState,
    onInsertCheckbox: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val defaultColor = MaterialTheme.colorScheme.onSurfaceVariant

    var showListMenu by remember { mutableStateOf(false) }

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
            // Bold
            item {
                IconButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
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

            // Italic
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

            // Underline
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

            // Strikethrough
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

            // H1
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

            // ✅ CHECKBOX - WIĘKSZA IKONA (28.dp zamiast 20.dp)
            item {
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }

// ✅ CHECKBOX
            item {
                IconButton(
                    onClick = {
                        // Wstaw marker checkboxa do HTML
                        val checkboxId = java.util.UUID.randomUUID().toString().take(8)
                        val currentHtml = state.toHtml()
                        val checkboxHtml = "<p><span data-checkbox='$checkboxId'>☐</span> </p>"
                        state.setHtml(currentHtml + checkboxHtml)
                        android.util.Log.d("FormattingToolbar", "Checkbox wstawiony: $checkboxId")
                    }
                ) {
                    Icon(
                        Icons.Default.CheckBox,
                        "Dodaj zadanie",
                        modifier = Modifier.size(28.dp),
                        tint = defaultColor
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

            // ✅ LISTY - ROZWIJANE MENU
            item {
                Box {
                    IconButton(onClick = { showListMenu = !showListMenu }) {
                        Icon(
                            Icons.Default.FormatListNumbered,
                            "Listy",
                            modifier = Modifier.size(20.dp),
                            tint = if (state.isOrderedList || state.isUnorderedList) primaryColor else defaultColor
                        )
                    }

                    DropdownMenu(
                        expanded = showListMenu,
                        onDismissRequest = { showListMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("1. Liczby (1, 2, 3...)") },
                            onClick = {
                                if (!state.isOrderedList) {
                                    state.toggleOrderedList()
                                }
                                showListMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("● Kropki") },
                            onClick = {
                                if (!state.isUnorderedList) {
                                    state.toggleUnorderedList()
                                }
                                showListMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}



// ✅ FUNKCJA POMOCNICZA - APLIKUJ STYLE LIST
// ✅ POPRAWIONA FUNKCJA - INLINE CSS W KAŻDYM <li>
// ✅ UPROSZCZONA FUNKCJA - TYLKO CO DZIAŁA
fun applyListStyle(state: com.mohamedrejeb.richeditor.model.RichTextState, style: ListStyle) {
    when (style) {
        ListStyle.ARABIC -> {
            if (!state.isOrderedList) {
                state.toggleOrderedList()
            }
            android.util.Log.d("ListStyle", "Włączono numerację 1, 2, 3...")
        }

        ListStyle.BULLET_DISC -> {
            if (!state.isUnorderedList) {
                state.toggleUnorderedList()
            }
            android.util.Log.d("ListStyle", "Włączono punktory ●")
        }

        ListStyle.NONE -> {
            if (state.isOrderedList) state.toggleOrderedList()
            if (state.isUnorderedList) state.toggleUnorderedList()
        }

        else -> {
            // Inne style nie są obsługiwane
            android.util.Log.w("ListStyle", "⚠️ Styl $style nie jest obsługiwany, używam ARABIC")
            if (!state.isOrderedList) {
                state.toggleOrderedList()
            }
        }
    }
}

// ✅ ENUM DLA STYLÓW LIST
// ✅ ENUM DLA STYLÓW LIST
// ✅ TYLKO DZIAŁAJĄCE OPCJE
enum class ListStyle {
    NONE,
    ARABIC,       // 1, 2, 3...
    BULLET_DISC   // ●
}

// ✅ POPRAWIONY TOOLBAR Z WSZYSTKIMI OPCJAMI
// ✅ UPROSZCZONY TOOLBAR - TYLKO CO DZIAŁA
@Composable
fun RichTextFormattingToolbar(
    state: com.mohamedrejeb.richeditor.model.RichTextState,

    currentListStyle: ListStyle,
    onListStyleChange: (ListStyle) -> Unit,
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
            // Bold
            item {
                IconButton(
                    onClick = {
                        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
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

            // Italic
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

            // Underline
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

            // Strikethrough
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

            // H1
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

            // ✅ CHECKBOX - zamiast H2
            item {
                IconButton(
                    onClick = {
                        onInsertCheckbox()
                        android.util.Log.d("Toolbar", "Checkbox kliknięty")
                    }
                ) {
                    Icon(
                        Icons.Default.CheckBox,
                        "Dodaj zadanie",
                        modifier = Modifier.size(20.dp),
                        tint = defaultColor
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

            // ✅ LISTA NUMEROWANA - TYLKO 1,2,3
            item {
                IconButton(
                    onClick = {
                        onListStyleChange(ListStyle.ARABIC)
                    }
                ) {
                    Icon(
                        Icons.Default.FormatListNumbered,
                        "Numerowanie (1, 2, 3...)",
                        modifier = Modifier.size(20.dp),
                        tint = if (currentListStyle == ListStyle.ARABIC) primaryColor else defaultColor
                    )
                }
            }

            // ✅ LISTA WYPUNKTOWANA - TYLKO ●
            item {
                IconButton(
                    onClick = {
                        onListStyleChange(ListStyle.BULLET_DISC)
                    }
                ) {
                    Icon(
                        Icons.Default.FormatListBulleted,
                        "Punktory (●)",
                        modifier = Modifier.size(20.dp),
                        tint = if (currentListStyle == ListStyle.BULLET_DISC) primaryColor else defaultColor
                    )
                }
            }
        }
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
@Composable
fun CheckboxTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    var localValue by remember(value) { mutableStateOf(value) }
    val checkboxes = remember(localValue.text) { parseCheckboxes(localValue.text) }

    Column(modifier = modifier) {
        // Renderuj linie z checkboxami
        val lines = localValue.text.split("\n")
        var currentLineStart = 0

        lines.forEachIndexed { index, line ->
            val lineStart = currentLineStart
            currentLineStart += line.length + 1

            when {
                line.startsWith(CHECKBOX_UNCHECKED) || line.startsWith(CHECKBOX_CHECKED) -> {
                    // ✅ CHECKBOX JAKO KOMPONENT UI
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = line.startsWith(CHECKBOX_CHECKED),
                            onCheckedChange = { checked ->
                                val newText = toggleCheckboxAtPosition(localValue.text, lineStart)
                                val newValue = TextFieldValue(
                                    text = newText,
                                    selection = localValue.selection
                                )
                                localValue = newValue
                                onValueChange(newValue)
                                android.util.Log.d("CheckboxTextField", "Checkbox przełączony na pozycji $lineStart -> $checked")
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Tekst obok checkboxa - edytowalny
                        TextField(
                            value = line.substring(1).trim(),
                            onValueChange = { newLineText ->
                                val checkboxSymbol = if (line.startsWith(CHECKBOX_CHECKED)) CHECKBOX_CHECKED else CHECKBOX_UNCHECKED
                                val newLine = "$checkboxSymbol $newLineText"

                                val newLines = lines.toMutableList()
                                newLines[index] = newLine
                                val newText = newLines.joinToString("\n")

                                val newValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(lineStart + newLineText.length)
                                )
                                localValue = newValue
                                onValueChange(newValue)
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = textStyle.copy(
                                textDecoration = if (line.startsWith(CHECKBOX_CHECKED))
                                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                                else null
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text("Wpisz zadanie...") }
                        )
                    }
                }
                else -> {
                    // Zwykły tekst - bez checkboxa
                    if (line.isNotEmpty() || index == lines.lastIndex) {
                        TextField(
                            value = line,
                            onValueChange = { newLineText ->
                                val newLines = lines.toMutableList()
                                newLines[index] = newLineText
                                val newText = newLines.joinToString("\n")

                                val newValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(lineStart + newLineText.length)
                                )
                                localValue = newValue
                                onValueChange(newValue)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = textStyle,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = if (index == 0 && line.isEmpty()) placeholder else null
                        )
                    }
                }
            }
        }
    }
}
// ✅ OVERLAY Z CHECKBOXAMI
@Composable
fun CheckboxOverlay(
    html: String,
    checkboxStates: Map<String, Boolean>,
    onCheckboxToggle: (String) -> Unit
) {
    // Parsuj HTML i znajdź checkboxy
    val checkboxPattern = Regex("""data-checkbox='([^']+)'>([☐☑])""")
    val matches = checkboxPattern.findAll(html)

    // Dla każdego checkboxa renderuj UI
    matches.forEach { match ->
        val checkboxId = match.groupValues[1]
        val isChecked = checkboxStates[checkboxId] ?: false

        // TODO: Oblicz pozycję checkboxa w tekście i wyrenderuj Checkbox na tej pozycji
        // To wymaga zaawansowanego layoutu - na razie renderujemy w Column
    }

    // UPROSZCZONA WERSJA - checkboxy u góry
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        checkboxStates.forEach { (checkboxId, isChecked) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clickable { onCheckboxToggle(checkboxId) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onCheckboxToggle(checkboxId) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4CAF50)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Zadanie $checkboxId",
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (isChecked)
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    else null
                )
            }
        }
    }
}