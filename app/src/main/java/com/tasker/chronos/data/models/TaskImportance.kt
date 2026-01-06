package com.tasker.chronos.data.models

import androidx.compose.ui.graphics.Color

// Przywracamy właściwość 'value' typu Int
enum class TaskImportance(val value: Int, val label: String, val color: Color) {
    Low(1, "Niski", Color(0xFF63F500)),
    Medium(2, "Średni", Color(0xFFF4C700)),
    High(3, "Wysoki", Color(0xFFF52600))
}
