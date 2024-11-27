package org.example.composeApp.component

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val progressColor = remember(animatedProgress) {
        getProgressColor(
            animatedProgress = animatedProgress,
            colors = listOf(
                0.3f to Color.Red,
                0.6f to Color.Yellow,
                1.0f to Color.Green
            )
        )
    }

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.height(Dimension.PROGRESS_BAR_HEIGHT.dp),
        color = progressColor,
    )
}
