package org.example.composeApp.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A composable to display a circular progress indicator with color transitions based on progress.
 *
 * @param progress The current progress value ranging from 0.0 to 1.0.
 * @param modifier Modifier to be applied to the progress bar.
 * @param size The size of the circular progress indicator.
 * @param strokeWidth The width of the circular progress indicator.
 * @param animationDuration The duration of the progress bar animation in milliseconds.
 * @param animationDelay The delay before the animation starts in milliseconds.
 * @param backgroundColor The color of the circular progress indicator background.
 * @param textStyle The style of the text displayed in the center of the circular progress indicator.
 */
@Composable
fun CustomCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 8.dp,
    animationDuration: Int = 1000,
    animationDelay: Int = 100,
    backgroundColor: Color = Color.LightGray,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var currentProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(progress) {
        currentProgress = progress
    }

    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay
        ),
        label = "CircularProgressIndicator"
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

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        listOf(
            backgroundColor to 360f,
            progressColor to (animatedProgress * 360)
        ).forEach { (color, sweepAngle) ->
            Canvas(modifier = Modifier.size(size)) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

        }
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}