// Plik: ui/components/ScreenNavigator.kt
package com.tasker.chronos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Uproszczony komponent, zawiera tylko dwie strzałki
@Composable
fun ArrowNavigator(
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    // Przycisk strzałki w lewo
    IconButton(onClick = onLeftClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Poprzedni ekran",
            tint = Color.Black
        )
    }
    // Przycisk strzałki w prawo
    IconButton(onClick = onRightClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Następny ekran",
            tint = Color.Black
        )
    }
}