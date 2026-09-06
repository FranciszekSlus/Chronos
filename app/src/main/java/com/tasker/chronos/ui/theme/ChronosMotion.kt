package com.tasker.chronos.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

/**
 * Shared motion specs for screen transitions and UI animations.
 */
object ChronosMotion {
    fun <T> tweenEnter(
        durationMillis: Int = 320,
        delayMillis: Int = 0,
        easing: Easing = FastOutSlowInEasing
    ): TweenSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = easing
    )

    fun <T> tweenExit(
        durationMillis: Int = 220,
        delayMillis: Int = 0,
        easing: Easing = LinearOutSlowInEasing
    ): TweenSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = easing
    )
}
