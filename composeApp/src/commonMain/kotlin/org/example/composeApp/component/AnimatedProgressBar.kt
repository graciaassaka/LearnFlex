package org.example.composeApp.component

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import org.example.composeApp.dimension.Dimension

/**
 * A composable to display an animated progress bar with color transitions based on progress.
 *
 * @param progress The current progress value ranging from 0.0 to 1.0.
 * @param modifier Modifier to be applied to the progress bar.
 * @param animationDuration The duration of the progress bar animation in milliseconds.
 * @param delay The delay before the animation starts in milliseconds.
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000,
    delay: Int = 100
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(progress) {
        currentProgress = progress
    }

    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = delay,
            easing = FastOutLinearInEasing
        ),
        label = "progressFloatAnimation"
    )

    val colors = listOf(
        0.3f to Color.Red,
        0.6f to Color.Yellow,
        1.0f to Color.Green
    )

    val progressColor = remember(animatedProgress) {
        when {
            animatedProgress <= colors.first().first -> colors.first().second
            animatedProgress >= colors.last().first -> colors.last().second
            else -> {
                val (colorStart, colorEnd) = colors.zipWithNext().first { (start, end) ->
                    animatedProgress in start.first..end.first
                }

                val segmentSize = colorEnd.first - colorStart.first
                val segmentProgress = (animatedProgress - colorStart.first) / segmentSize

                lerp(
                    start = colorStart.second,
                    stop = colorEnd.second,
                    fraction = segmentProgress
                )
            }
        }
    }

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.height(Dimension.PROGRESS_BAR_HEIGHT.dp),
        color = progressColor,
    )
}
