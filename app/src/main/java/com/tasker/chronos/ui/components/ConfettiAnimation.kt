// Plik: ui/components/ConfettiAnimation.kt
package com.tasker.chronos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float, // Prędkość pozioma (wybuch)
    val velocityY: Float, // Prędkość pionowa (wybuch w górę, potem grawitacja)
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val gravity: Float = 800f // Grawitacja
)

@Composable
fun ConfettiAnimation(onAnimationEnd: () -> Unit) {
    val particles = remember {
        List(100) {
            val angle = Random.nextFloat() * 360f
            val speed = Random.nextFloat() * 600f + 300f
            val radians = Math.toRadians(angle.toDouble())

            ConfettiParticle(
                initialX = 0.5f, // Start ze środka
                initialY = 0.5f, // Start ze środka
                velocityX = (cos(radians) * speed).toFloat(),
                velocityY = (sin(radians) * speed).toFloat() - 400f, // Dodatkowy impuls w górę
                color = listOf(
                    Color(0xFFFF6B35),
                    Color(0xFF4CAF50),
                    Color(0xFF2196F3),
                    Color(0xFFFFEB3B),
                    Color(0xFFE91E63),
                    Color(0xFF9C27B0),
                    Color(0xFFFF9800),
                    Color(0xFF00BCD4)
                ).random(),
                size = Random.nextFloat() * 10f + 6f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
                gravity = Random.nextFloat() * 200f + 600f
            )
        }
    }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
        )
        delay(100)
        onAnimationEnd()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val progress = animationProgress.value
        val dt = progress // Delta time (0 to 1)

        particles.forEach { particle ->
            // Fizyka ruchu z grawitacją
            val x = size.width * particle.initialX + particle.velocityX * dt
            val y = size.height * particle.initialY +
                    particle.velocityY * dt +
                    0.5f * particle.gravity * dt * dt // Równanie ruchu z grawitacją

            // Rysuj tylko jeśli nie wyleciało za ekran (z marginesem)
            if (y < size.height + 100 && x > -100 && x < size.width + 100) {
                val rotation = particle.rotationSpeed * dt
                val alpha = (1f - progress * 0.4f).coerceIn(0f, 1f)

                rotate(degrees = rotation, pivot = Offset(x, y)) {
                    // Prostokątne konfetti
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(x - particle.size / 2, y - particle.size / 2),
                        size = Size(particle.size, particle.size * 1.5f)
                    )
                }
            }
        }
    }
}