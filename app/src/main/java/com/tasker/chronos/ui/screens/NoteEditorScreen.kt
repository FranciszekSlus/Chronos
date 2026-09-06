package com.tasker.chronos.ui.screens

import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tasker.chronos.data.models.Note
import com.tasker.chronos.data.models.NoteCategory
import com.tasker.chronos.utils.NoteCrypto
import java.time.format.DateTimeFormatter
import java.util.UUID

private data class NoteChecklistItem(
    val id: String,
    val label: String,
    val checked: Boolean
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: Note?,
    categories: List<NoteCategory>,
    onSave: (Note) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onTogglePin: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(note?.title.orEmpty()) }
    var selectedCategoryId by remember { mutableStateOf(note?.categoryId) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showFormattingToolbar by remember { mutableStateOf(true) }
    var showLockDialog by remember { mutableStateOf(false) }
    var lockPassword by remember { mutableStateOf("") }
    var lockPasswordConfirm by remember { mutableStateOf("") }
    var unlockPassword by remember { mutableStateOf("") }
    var lockError by remember { mutableStateOf<String?>(null) }
    var isLocked by remember { mutableStateOf(note?.isLocked == true) }
    var unlocked by remember { mutableStateOf(note?.isLocked != true) }
    var checklist by remember { mutableStateOf<List<NoteChecklistItem>>(emptyList()) }
    var selectionFontSp by remember { mutableFloatStateOf(18f) }
    var bodyHtml by remember { mutableStateOf("") }
    var editTextRef by remember { mutableStateOf<EditText?>(null) }

    LaunchedEffect(note?.id) {
        if (note?.isLocked == true && NoteCrypto.isEncrypted(note.content)) {
            unlocked = false
            checklist = emptyList()
            bodyHtml = ""
        } else {
            unlocked = true
            val html = note?.content.orEmpty()
            checklist = parseNoteChecklist(html)
            bodyHtml = stripNoteChecklist(html)
        }
    }

    fun buildHtml(): String {
        val listHtml = checklist.joinToString("") { item ->
            val mark = if (item.checked) "\u2611" else "\u2610"
            val safe = item.label
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
            "<p><span data-checkbox='${item.id}'>$mark</span> $safe</p>"
        }
        val fromEditor = editTextRef?.let { spannableToHtml(it.text) } ?: bodyHtml
        return listHtml + fromEditor
    }

    fun saveNote(locked: Boolean = isLocked, encrypted: String? = null) {
        if (title.isBlank()) return
        onSave(
            note?.copy(
                title = title.trim(),
                content = encrypted ?: buildHtml(),
                categoryId = selectedCategoryId,
                isLocked = locked
            ) ?: Note(
                title = title.trim(),
                content = encrypted ?: buildHtml(),
                categoryId = selectedCategoryId,
                isLocked = locked
            )
        )
    }

    if (!unlocked && note?.isLocked == true) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text("Zablokowana notatka") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Podaj hasło, żeby otworzyć notatkę.")
                    OutlinedTextField(
                        value = unlockPassword,
                        onValueChange = {
                            unlockPassword = it
                            lockError = null
                        },
                        label = { Text("Hasło") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    lockError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val plain = NoteCrypto.decrypt(note.content, unlockPassword)
                        if (plain == null) {
                            lockError = "Złe hasło"
                        } else {
                            checklist = parseNoteChecklist(plain)
                            bodyHtml = stripNoteChecklist(plain)
                            unlocked = true
                            unlockPassword = ""
                        }
                    }
                ) { Text("Odblokuj") }
            },
            dismissButton = {
                TextButton(onClick = onCancel) { Text("Anuluj") }
            }
        )
        return
    }

    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
    val hintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f).toArgb()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (note != null) {
                        Text(
                            note.updatedAt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text("Nowa notatka")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showFormattingToolbar = !showFormattingToolbar }) {
                        Icon(
                            Icons.Default.FormatSize,
                            contentDescription = "Formatowanie",
                            tint = if (showFormattingToolbar)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showLockDialog = true }) {
                        Icon(
                            if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Hasło",
                            tint = if (isLocked) Color(0xFFFF6B35) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (note != null && onTogglePin != null) {
                        IconButton(onClick = onTogglePin) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Przypnij",
                                tint = if (note.isPinned) Color(0xFFFF6B35) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = { showCategoryPicker = true }) {
                        Icon(Icons.Default.Category, contentDescription = "Kategoria")
                    }
                    if (onDelete != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Usuń",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (isLocked) showLockDialog = true else saveNote(locked = false)
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Zapisz")
                    }
                },
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
        ) {
            if (showFormattingToolbar) {
                NoteNativeFormattingToolbar(
                    editText = editTextRef,
                    selectionFontSp = selectionFontSp,
                    onSelectionFontSpChange = { selectionFontSp = it },
                    onInsertCheckbox = {
                        checklist = checklist + NoteChecklistItem(
                            id = UUID.randomUUID().toString().take(8),
                            label = "",
                            checked = false
                        )
                    }
                )
                HorizontalDivider()
            }

            selectedCategoryId?.let { id ->
                categories.find { it.id == id }?.let { category ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.15f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            category.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(android.graphics.Color.parseColor(category.color)),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text("Tytuł", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

            if (checklist.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    checklist.forEachIndexed { index, item ->
                        key(item.id) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = item.checked,
                                    onCheckedChange = { checked ->
                                        checklist = checklist.toMutableList().also {
                                            it[index] = item.copy(checked = checked)
                                        }
                                    }
                                )
                                TextField(
                                    value = item.label,
                                    onValueChange = { text ->
                                        checklist = checklist.toMutableList().also {
                                            it[index] = item.copy(label = text)
                                        }
                                    },
                                    placeholder = { Text("Pozycja listy…") },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = if (item.checked) TextDecoration.LineThrough else null
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { checklist = checklist.filterNot { it.id == item.id } }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Usuń")
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Natywny EditText = poprawny uchwyt zaznaczenia + Spannable formatowanie
            key(note?.id, unlocked, bodyHtml.isEmpty()) {
                NoteSpannableEditor(
                    html = bodyHtml,
                    onHtmlChange = { bodyHtml = it },
                    onEditTextReady = { editTextRef = it },
                    textColor = onSurface,
                    hintColor = hintColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }

    if (showLockDialog) {
        AlertDialog(
            onDismissRequest = { showLockDialog = false; lockError = null },
            title = { Text(if (isLocked) "Zapisz z hasłem" else "Zabezpiecz hasłem") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Proste szyfrowanie treści notatki hasłem.")
                    OutlinedTextField(
                        value = lockPassword,
                        onValueChange = { lockPassword = it; lockError = null },
                        label = { Text("Hasło") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (!isLocked) {
                        OutlinedTextField(
                            value = lockPasswordConfirm,
                            onValueChange = { lockPasswordConfirm = it; lockError = null },
                            label = { Text("Powtórz hasło") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }
                    lockError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (isLocked) {
                        TextButton(
                            onClick = {
                                isLocked = false
                                showLockDialog = false
                                saveNote(locked = false)
                            }
                        ) { Text("Usuń blokadę (zapisz jawnie)") }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (lockPassword.length < 4) {
                            lockError = "Hasło min. 4 znaki"
                            return@TextButton
                        }
                        if (!isLocked && lockPassword != lockPasswordConfirm) {
                            lockError = "Hasła nie są takie same"
                            return@TextButton
                        }
                        val encrypted = NoteCrypto.encrypt(buildHtml(), lockPassword)
                        isLocked = true
                        showLockDialog = false
                        lockPassword = ""
                        lockPasswordConfirm = ""
                        saveNote(locked = true, encrypted = encrypted)
                    }
                ) { Text("Zapisz zaszyfrowane") }
            },
            dismissButton = {
                TextButton(onClick = { showLockDialog = false }) { Text("Anuluj") }
            }
        )
    }

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
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text("Brak kategorii", modifier = Modifier.padding(16.dp))
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
                            else MaterialTheme.colorScheme.surfaceVariant
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
                                Text(category.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) { Text("Zamknij") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Usuń notatkę?") },
            text = { Text("Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete?.invoke() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
private fun NoteNativeFormattingToolbar(
    editText: EditText?,
    selectionFontSp: Float,
    onSelectionFontSpChange: (Float) -> Unit,
    onInsertCheckbox: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        FormatIconButton(
            icon = Icons.Default.CheckBox,
            selected = false,
            selectedTint = primary,
            defaultTint = muted,
            contentDescription = "Checkbox"
        ) { onInsertCheckbox() }

        FormatIconButton(
            icon = Icons.Default.FormatBold,
            selected = false,
            selectedTint = primary,
            defaultTint = muted,
            contentDescription = "Pogrubienie"
        ) { editText?.toggleStyleSpan(Typeface.BOLD) }

        FormatIconButton(
            icon = Icons.Default.FormatItalic,
            selected = false,
            selectedTint = primary,
            defaultTint = muted,
            contentDescription = "Kursywa"
        ) { editText?.toggleStyleSpan(Typeface.ITALIC) }

        FormatIconButton(
            icon = Icons.Default.FormatUnderlined,
            selected = false,
            selectedTint = primary,
            defaultTint = muted,
            contentDescription = "Podkreślenie"
        ) { editText?.toggleUnderline() }

        FormatIconButton(
            icon = Icons.Default.FormatStrikethrough,
            selected = false,
            selectedTint = primary,
            defaultTint = muted,
            contentDescription = "Przekreślenie"
        ) { editText?.toggleStrikethrough() }

        Spacer(modifier = Modifier.size(8.dp))

        IconButton(
            onClick = {
                val next = (selectionFontSp - 2f).coerceAtLeast(12f)
                onSelectionFontSpChange(next)
                editText?.applyAbsoluteSizeSp(next)
            }
        ) {
            Icon(Icons.Default.TextDecrease, contentDescription = "Zmniejsz zaznaczenie")
        }
        Text(
            "${selectionFontSp.toInt()}",
            style = MaterialTheme.typography.labelMedium,
            color = muted
        )
        IconButton(
            onClick = {
                val next = (selectionFontSp + 2f).coerceAtMost(36f)
                onSelectionFontSpChange(next)
                editText?.applyAbsoluteSizeSp(next)
            }
        ) {
            Icon(Icons.Default.TextIncrease, contentDescription = "Powiększ zaznaczenie")
        }
    }
}

@Composable
private fun FormatIconButton(
    icon: ImageVector,
    selected: Boolean,
    selectedTint: Color,
    defaultTint: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (selected) selectedTint else defaultTint
        )
    }
}

@Composable
private fun NoteSpannableEditor(
    html: String,
    onHtmlChange: (String) -> Unit,
    onEditTextReady: (EditText) -> Unit,
    textColor: Int,
    hintColor: Int,
    modifier: Modifier = Modifier
) {
    val watcher = remember {
        object : TextWatcher {
            var suppress = false
            var callback: ((String) -> Unit)? = null
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!suppress) callback?.invoke(spannableToHtml(s))
            }
        }
    }
    watcher.callback = onHtmlChange

    AndroidView(
        factory = { context ->
            EditText(context).apply {
                hint = "Zaznacz tekst i użyj paska formatowania"
                setHintTextColor(hintColor)
                setTextColor(textColor)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                gravity = Gravity.TOP or Gravity.START
                includeFontPadding = true
                setLineSpacing(0f, 1.2f)
                setPadding(0, 8, 0, 8)
                setText(htmlToSpannable(html), android.widget.TextView.BufferType.SPANNABLE)
                addTextChangedListener(watcher)
                onEditTextReady(this)
            }
        },
        update = { editText ->
            editText.setTextColor(textColor)
            onEditTextReady(editText)
            val current = spannableToHtml(editText.text)
            if (current != html) {
                // Only reload when content was set externally (unlock / open note)
                val plainCurrent = editText.text?.toString().orEmpty()
                val plainIncoming = htmlToSpannable(html).toString()
                if (plainCurrent != plainIncoming && editText.text.isNullOrEmpty()) {
                    watcher.suppress = true
                    editText.setText(htmlToSpannable(html), android.widget.TextView.BufferType.SPANNABLE)
                    watcher.suppress = false
                }
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose { watcher.callback = null }
    }
}

private fun EditText.selectionRange(): IntRange? {
    val start = selectionStart
    val end = selectionEnd
    if (start < 0 || end < 0 || start == end) return null
    return minOf(start, end) until maxOf(start, end)
}

private fun EditText.toggleStyleSpan(style: Int) {
    val range = selectionRange() ?: return
    val editable = text as? Spannable ?: return
    val existing = editable.getSpans(range.first, range.last + 1, StyleSpan::class.java)
        .filter { it.style == style }
    if (existing.isNotEmpty()) {
        existing.forEach { editable.removeSpan(it) }
    } else {
        editable.setSpan(
            StyleSpan(style),
            range.first,
            range.last + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

private fun EditText.toggleUnderline() {
    val range = selectionRange() ?: return
    val editable = text as? Spannable ?: return
    val existing = editable.getSpans(range.first, range.last + 1, UnderlineSpan::class.java)
    if (existing.isNotEmpty()) {
        existing.forEach { editable.removeSpan(it) }
    } else {
        editable.setSpan(
            UnderlineSpan(),
            range.first,
            range.last + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

private fun EditText.toggleStrikethrough() {
    val range = selectionRange() ?: return
    val editable = text as? Spannable ?: return
    val existing = editable.getSpans(range.first, range.last + 1, StrikethroughSpan::class.java)
    if (existing.isNotEmpty()) {
        existing.forEach { editable.removeSpan(it) }
    } else {
        editable.setSpan(
            StrikethroughSpan(),
            range.first,
            range.last + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

private fun EditText.applyAbsoluteSizeSp(sp: Float) {
    val range = selectionRange() ?: return
    val editable = text as? Spannable ?: return
    editable.getSpans(range.first, range.last + 1, AbsoluteSizeSpan::class.java)
        .forEach { editable.removeSpan(it) }
    editable.setSpan(
        AbsoluteSizeSpan(sp.toInt().coerceAtLeast(8), true),
        range.first,
        range.last + 1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

@Suppress("DEPRECATION")
private fun htmlToSpannable(html: String): SpannableStringBuilder {
    if (html.isBlank()) return SpannableStringBuilder("")
    val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(html)
    }
    return SpannableStringBuilder(spanned)
}

@Suppress("DEPRECATION")
private fun spannableToHtml(text: CharSequence?): String {
    if (text.isNullOrEmpty()) return ""
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.toHtml(SpannableStringBuilder(text), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    } else {
        Html.toHtml(SpannableStringBuilder(text))
    }
}

private fun parseNoteChecklist(html: String): List<NoteChecklistItem> {
    if (html.isBlank()) return emptyList()
    val regex = Regex(
        """<span\s+data-checkbox=['"]([^'"]+)['"]\s*>(\u2610|\u2611)</span>\s*([^<]*)""",
        RegexOption.IGNORE_CASE
    )
    return regex.findAll(html).map { match ->
        NoteChecklistItem(
            id = match.groupValues[1],
            checked = match.groupValues[2] == "\u2611",
            label = match.groupValues[3]
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim()
        )
    }.toList()
}

private fun stripNoteChecklist(html: String): String {
    if (html.isBlank()) return html
    return html.replace(
        Regex(
            """<p>\s*<span\s+data-checkbox=['"][^'"]+['"]\s*>(\u2610|\u2611)</span>\s*[^<]*</p>""",
            setOf(RegexOption.IGNORE_CASE)
        ),
        ""
    ).trim()
}
