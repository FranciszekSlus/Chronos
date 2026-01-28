package com.tasker.chronos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// ✅ Markery dla checkboxów w tekście
const val CHECKBOX_UNCHECKED = "☐ "
const val CHECKBOX_CHECKED = "☑ "

@Composable
fun CheckboxRichTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onInsertCheckbox: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null
) {
    var internalValue by remember(value) { mutableStateOf(value) }

    // Wykryj kliknięcie w checkbox
    val onTextClick: (Int) -> Unit = { offset ->
        val text = internalValue.text

        // Sprawdź czy kliknięto w checkbox (2 znaki przed offsetem)
        if (offset >= 2) {
            val possibleCheckbox = text.substring(maxOf(0, offset - 2), offset)

            when {
                possibleCheckbox == CHECKBOX_UNCHECKED -> {
                    // Zamień na checked
                    val newText = text.replaceRange(offset - 2, offset, CHECKBOX_CHECKED)
                    val newValue = internalValue.copy(
                        text = newText,
                        selection = TextRange(offset)
                    )
                    internalValue = newValue
                    onValueChange(newValue)
                    android.util.Log.d("CheckboxTextField", "Zaznaczono checkbox na pozycji ${offset - 2}")
                }
                possibleCheckbox == CHECKBOX_CHECKED -> {
                    // Zamień na unchecked
                    val newText = text.replaceRange(offset - 2, offset, CHECKBOX_UNCHECKED)
                    val newValue = internalValue.copy(
                        text = newText,
                        selection = TextRange(offset)
                    )
                    internalValue = newValue
                    onValueChange(newValue)
                    android.util.Log.d("CheckboxTextField", "Odznaczono checkbox na pozycji ${offset - 2}")
                }
            }
        }
    }

    BasicTextField(
        value = internalValue,
        onValueChange = { newValue ->
            internalValue = newValue
            onValueChange(newValue)
        },
        modifier = modifier.fillMaxSize(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (internalValue.text.isEmpty() && placeholder != null) {
                    placeholder()
                }
                innerTextField()
            }
        }
    )
}