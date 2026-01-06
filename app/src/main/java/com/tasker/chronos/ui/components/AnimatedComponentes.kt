// Plik: ui/components/AnimatedComponents.kt
package com.tasker.chronos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Glassmorphism Card - efekt szkła
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = {
                        isPressed = true
                        onClick()
                    })
                } else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(content = content)
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

/**
 * Pulsujący FAB
 */
@Composable
fun PulsatingFAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        icon()
    }
}

/**
 * Shimmer effect - loading placeholder
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.LightGray.copy(alpha = shimmerAlpha),
                        Color.Gray.copy(alpha = shimmerAlpha),
                        Color.LightGray.copy(alpha = shimmerAlpha)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
    )
}

/**
 * Bounce animation przy kliknięciu
 */
@Composable
fun BounceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp)
    ) {
        content()
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

/**
 * Gradient background z animacją
 */
@Composable
fun AnimatedGradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = androidx.compose.ui.geometry.Offset(0f, offset * 1000),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f + offset * 1000)
                )
            )
    ) {
        content()
    }
}

/**
 * Floating card - unosi się nad innymi
 */
@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    elevation: Float = 8f,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Particle explosion effect
 */
@Composable
fun ParticleExplosion(
    isExploding: Boolean,
    onComplete: () -> Unit = {}
) {
    if (!isExploding) return

    val particles = remember {
        List(20) {
            ParticleData(
                angle = (it * 360f / 20f),
                distance = 100f + kotlin.random.Random.nextFloat() * 50f,
                color = listOf(
                    Color(0xFFFF6B35),
                    Color(0xFFFFC107),
                    Color(0xFF4CAF50),
                    Color(0xFF2196F3)
                ).random()
            )
        }
    }

    particles.forEach { particle ->
        ParticleItem(particle = particle, onComplete = onComplete)
    }
}

data class ParticleData(
    val angle: Float,
    val distance: Float,
    val color: Color
)

@Composable
fun ParticleItem(
    particle: ParticleData,
    onComplete: () -> Unit
) {
    val animatedDistance = remember { Animatable(0f) }
    val animatedAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            animatedDistance.animateTo(
                targetValue = particle.distance,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(300)
            animatedAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(200)
            )
            onComplete()
        }
    }

    val radians = Math.toRadians(particle.angle.toDouble())
    val x = (animatedDistance.value * kotlin.math.cos(radians)).toFloat()
    val y = (animatedDistance.value * kotlin.math.sin(radians)).toFloat()

    Box(
        modifier = Modifier
            .offset(x.dp, y.dp)
            .size(8.dp)
            .alpha(animatedAlpha.value)
            .background(particle.color, CircleShape)
    )
}

/**
 * Smooth fade in/out transitions
 */
@Composable
fun FadeInContent(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
    ) {
        content()
    }
}

/**
 * Success checkmark animation
 */
@Composable
fun AnimatedCheckmark(
    isChecked: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val rotation by animateFloatAsState(
        targetValue = if (isChecked) 0f else -180f,
        animationSpec = tween(300)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
            contentDescription = "Completed",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(48.dp)
        )
    }

}
@Composable
fun AnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 4.dp
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text, fontWeight = FontWeight.Medium)
        }
    }
}
