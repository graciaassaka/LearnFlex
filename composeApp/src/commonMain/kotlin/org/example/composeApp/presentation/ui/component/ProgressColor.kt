package org.example.composeApp.presentation.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Returns the color based on the progress value.
 *
 * @param animatedProgress The current progress value ranging from 0.0 to 1.0.
 * @param colors The list of color pairs with progress value and color.
 * @return The color based on the progress value.
 */
fun getProgressColor(animatedProgress: Float, colors: List<Pair<Float, Color>>): Color =
    when {
        colors.isEmpty()                                        -> Color.Gray
        animatedProgress <= (colors.firstOrNull()?.first ?: 0f) -> colors.firstOrNull()?.second ?: Color.Gray
        animatedProgress >= (colors.lastOrNull()?.first ?: 1f)  -> colors.lastOrNull()?.second ?: Color.Gray
        else                                                    -> {
            val (colorStart, colorEnd) = colors.zipWithNext().firstOrNull { (start, end) ->
                animatedProgress in start.first..end.first
            } ?: return Color.Gray

            val segmentSize = colorEnd.first - colorStart.first
            val segmentProgress = (animatedProgress - colorStart.first) / segmentSize

            lerp(
                start = colorStart.second,
                stop = colorEnd.second,
                fraction = segmentProgress
            )
        }
    }