
package com.tasker.chronos.ui.theme


import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.WindowCompat

// ✅ Paleta kolorów dla Light Theme (Blue Wave)
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,              // #0272ED
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,

    secondary = BluePrimaryDark,        // #00695C (teal/navy)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00201B),

    tertiary = GoalGold,                // Złoty dla celów
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE082),
    onTertiaryContainer = Color(0xFF4A2800),

    error = PriorityHigh,               // Czerwony
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFCFCFC),
    onBackground = Color(0xFF1A1C1E),

    surface = SurfaceLight,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),

    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
)

// ✅ Paleta kolorów dla Dark Theme (Blue Wave)
private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryLight,         // Jaśniejszy niebieski dla dark mode
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFD0E8FF),

    secondary = Color(0xFF80CBC4),      // Jaśniejszy teal dla dark mode
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF005045),
    onSecondaryContainer = Color(0xFFB2DFDB),

    tertiary = Color(0xFFFFD54F),       // Jaśniejszy złoty dla dark mode
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF6A3C00),
    onTertiaryContainer = Color(0xFFFFE082),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),

    surface = SurfaceDark,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),

    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
)

// ✅ POMOCNICZA FUNKCJA: Gradient Brush dla Blue Wave
fun blueWaveGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF004AF3),  // ✅ Nowy start
            Color(0xFF002C8F)    // ✅ Deep Indigo (Blue Twilight)
        )
    )
}

@Composable
fun ChronosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // Dynamic color dla Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

fun textWithBlueGlow(): TextStyle {
    return TextStyle(
        shadow = Shadow(
            color = Color(0x4D0064FF),  // rgba(0, 100, 255, 0.3)
            offset = Offset(0f, 2f),
            blurRadius = 15f
        )
    )
}

// ═══════════════════════════════════════════════════════════════
// PRZYKŁAD UŻYCIA GRADIENTU:

// W FAB:
// FloatingActionButton(
//     onClick = { ... },
//     containerColor = Color.Transparent,  // Ustaw transparent
//     modifier = Modifier.background(
//         brush = blueWaveGradient(),
//         shape = CircleShape
//     )
// ) { ... }

// W Button:
// Button(
//     onClick = { ... },
//     colors = ButtonDefaults.buttonColors(
//         containerColor = Color.Transparent
//     ),
//     modifier = Modifier.background(
//         brush = blueWaveGradient(),
//         shape = RoundedCornerShape(8.dp)
//     )
// ) { ... }

// W Card:
// Card(
//     colors = CardDefaults.cardColors(
//         containerColor = Color.Transparent
//     ),
//     modifier = Modifier.background(
//         brush = blueWaveGradient(),
//         shape = RoundedCornerShape(12.dp)
//     )
// ) { ... }