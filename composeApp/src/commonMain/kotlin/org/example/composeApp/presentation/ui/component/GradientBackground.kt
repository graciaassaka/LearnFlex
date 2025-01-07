package org.example.composeApp.presentation.ui.component

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.sin

/**
 * A composable function that renders a canvas with an animated linear gradient background.
 *
 * @param color1 The first color in the gradient.
 * @param color2 The second color in the gradient.
 * @param modifier The modifier to be applied to the canvas, allowing for layout adjustments.
 */
@Composable
fun GradientBackground(
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier
) {
    val time by produceState(0f) { while (true) withInfiniteAnimationFrameMillis { value = it / 1000f } }
    Canvas(modifier = modifier.fillMaxSize()) {
        val fraction = (sin(time) + 1) / 2

        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                color1.copy(alpha = 0.6f + 0.4f * fraction),
                color2.copy(alpha = 0.6f + 0.4f * fraction)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )

        drawRect(brush = gradientBrush)
    }
}