package com.tasker.chronos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PastelPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = PastelPrimaryContainer,
    onPrimaryContainer = PastelOnPrimaryContainer,

    secondary = PastelSecondaryDark,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = PastelSecondaryContainer,
    onSecondaryContainer = PastelOnSecondaryContainer,

    tertiary = PastelTertiary,
    onTertiary = Color(0xFF1A3D36),
    tertiaryContainer = PastelTertiaryContainer,
    onTertiaryContainer = PastelOnTertiaryContainer,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color.Transparent,
    onBackground = Color(0xFF2A2D38),

    surface = Color(0xF2FFFFFF),
    onSurface = Color(0xFF2A2D38),
    surfaceVariant = Color(0xFFE8ECF5),
    onSurfaceVariant = Color(0xFF5C6070),

    outline = Color(0xFFC8CCDA),
    outlineVariant = Color(0xFFE0E3EE),
)

private val DarkColorScheme = darkColorScheme(
    primary = PastelPrimaryLight,
    onPrimary = Color(0xFF1A1F33),
    primaryContainer = Color(0xFF3D4566),
    onPrimaryContainer = Color(0xFFE8ECFF),

    secondary = PastelSecondary,
    onSecondary = Color(0xFF3D1F2A),
    secondaryContainer = Color(0xFF5A3D4A),
    onSecondaryContainer = Color(0xFFFFE4EE),

    tertiary = PastelTertiary,
    onTertiary = Color(0xFF0D2620),
    tertiaryContainer = Color(0xFF2A4A42),
    onTertiaryContainer = Color(0xFFDFF7F0),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color.Transparent,
    onBackground = Color(0xFFE8E6EF),

    surface = Color(0xE6282C38),
    onSurface = Color(0xFFE8E6EF),
    surfaceVariant = Color(0xFF3A3F4D),
    onSurfaceVariant = Color(0xFFC4C6D0),

    outline = Color(0xFF6B7080),
    outlineVariant = Color(0xFF454A58),
)

fun chronosAmbientBackgroundBrush(darkTheme: Boolean): Brush {
    return if (darkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF252A38),
                Color(0xFF1C212E),
                Color(0xFF161B24)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF5F3FF),
                Color(0xFFFFF8FC),
                Color(0xFFEFFAF6)
            )
        )
    }
}

/** Gradient do przycisków / akcentów (pastelowy) */
fun chronosAccentGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(PastelPrimary, PastelSecondaryDark, PastelTertiary)
    )
}

/** Alias dla istniejących wywołań w UI */
fun blueWaveGradient(): Brush = chronosAccentGradient()

fun textWithBlueGlow(): TextStyle {
    return TextStyle(
        shadow = Shadow(
            color = PastelPrimary.copy(alpha = 0.35f),
            offset = Offset(0f, 2f),
            blurRadius = 12f
        )
    )
}

fun textWithGreenGlow(): TextStyle {
    return TextStyle(
        shadow = Shadow(
            color = Color(0xFF4CAF50).copy(alpha = 0.35f),
            offset = Offset(0f, 2f),
            blurRadius = 12f
        )
    )
}

fun textWithGoldGlow(): TextStyle {
    return TextStyle(
        shadow = Shadow(
            color = Color(0xFFFFD700).copy(alpha = 0.35f),
            offset = Offset(0f, 2f),
            blurRadius = 12f
        )
    )
}

@Composable
fun ChronosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Wyłączone domyślnie: Material You nadpisuje pastelową paletę na Androidzie 12+. */
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
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
        shapes = ChronosShapes,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(chronosAmbientBackgroundBrush(darkTheme))
        ) {
            content()
        }
    }
}
