package com.tasker.chronos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp


data class CheckboxInText(
    val position: Int,
    val isChecked: Boolean,
    val lineText: String,
    val lineStart: Int
)

// Parser checkboxów z tekstu
fun parseCheckboxes(text: String): List<CheckboxInText> {
    val checkboxes = mutableListOf<CheckboxInText>()
    val lines = text.split("\n")
    var currentPosition = 0

    lines.forEachIndexed { lineIndex, line ->
        when {
            line.startsWith(CHECKBOX_UNCHECKED) -> {
                checkboxes.add(
                    CheckboxInText(
                        position = currentPosition,
                        isChecked = false,
                        lineText = line.substring(1).trim(),
                        lineStart = currentPosition
                    )
                )
            }
            line.startsWith(CHECKBOX_CHECKED) -> {
                checkboxes.add(
                    CheckboxInText(
                        position = currentPosition,
                        isChecked = true,
                        lineText = line.substring(1).trim(),
                        lineStart = currentPosition
                    )
                )
            }
        }
        currentPosition += line.length + 1 // +1 dla \n
    }

    return checkboxes
}

// Zamień checkbox w tekście
fun toggleCheckboxAtPosition(text: String, position: Int): String {
    val lines = text.split("\n")
    var currentPos = 0
    val newLines = lines.map { line ->
        val lineStart = currentPos
        val lineEnd = currentPos + line.length
        currentPos = lineEnd + 1 // +1 dla \n

        when {
            position >= lineStart && position <= lineEnd -> {
                when {
                    line.startsWith(CHECKBOX_UNCHECKED) ->
                        CHECKBOX_CHECKED + line.substring(1)
                    line.startsWith(CHECKBOX_CHECKED) ->
                        CHECKBOX_UNCHECKED + line.substring(1)
                    else -> line
                }
            }
            else -> line
        }
    }

    return newLines.joinToString("\n")
}

