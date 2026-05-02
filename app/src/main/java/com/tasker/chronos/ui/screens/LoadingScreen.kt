// Plik: ui/components/LoadingScreen.kt
package com.tasker.chronos.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasker.chronos.ui.theme.BluePrimary
import com.tasker.chronos.ui.theme.BluePrimaryLight
import com.tasker.chronos.ui.theme.PastelSecondaryDark
import com.tasker.chronos.ui.theme.PastelTertiary

@Composable
fun LoadingScreen() {
    // Animacja pulsowania tekstu
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlpha"
    )

    val dotsScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotsScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F3FF),
                        Color(0xFFFFF5FB),
                        Color(0xFFEFFAF6)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // ✅ TWOJA ANIMACJA - CircularWaveAnimation
            CircularWaveAnimation()

            // ✅ Tekst "Ładowanie" z text-shadow (blue glow)
            Text(
                text = "Ładowanie",
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2A2D38),
                    shadow = Shadow(
                        color = BluePrimaryLight.copy(alpha = 0.7f),
                        offset = Offset(0f, 4f),
                        blurRadius = 25f
                    )
                ),
                modifier = Modifier.alpha(textAlpha)
            )

            // ✅ Animowane kropki "..."
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.scale(dotsScale)
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .alpha(dotAlpha)
                            .background(
                                color = BluePrimaryLight,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
    }
}

/**
 * ✅ ZAPĘTLONA ANIMACJA FAL (3 pulsujące okręgi)
 */
@Composable
fun CircularWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "waves")

    // 3 fale z różnymi prędkościami
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave3"
    )

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Fala 1 (największa - zewnętrzna)
        WaveCircle(
            progress = wave1,
            maxSize = 140.dp,
            color = BluePrimaryLight.copy(alpha = 0.25f)
        )

        // Fala 2 (środkowa)
        WaveCircle(
            progress = wave2,
            maxSize = 110.dp,
            color = BluePrimary.copy(alpha = 0.35f)
        )

        // Fala 3 (wewnętrzna)
        WaveCircle(
            progress = wave3,
            maxSize = 80.dp,
            color = PastelSecondaryDark.copy(alpha = 0.35f)
        )

        // ✅ Centralny pulsujący punkt (rdzeń)
        val coreScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "core"
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .scale(coreScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BluePrimaryLight,
                            PastelTertiary
                        )
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}

/**
 * Pojedyncza fala (okrąg rozszerzający się i zanikający)
 */
@Composable
fun WaveCircle(
    progress: Float,
    maxSize: androidx.compose.ui.unit.Dp,
    color: Color
) {
    val size = maxSize * progress
    val alpha = 1f - progress  // Zanika w miarę rozszerzania

    Box(
        modifier = Modifier
            .size(size)
            .alpha(alpha)
            .background(
                color = color,
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}