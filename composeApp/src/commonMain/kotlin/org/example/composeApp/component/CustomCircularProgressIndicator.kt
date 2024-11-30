package org.example.composeApp.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
 * A custom circular progress indicator that displays the progress of a task.
 *
 * @param label The label to display below the progress value.
 * @param progress The progress value to display.
 * @param modifier The modifier to apply to the progress indicator.
 * @param size The size of the progress indicator.
 * @param strokeWidth The width of the progress indicator's stroke.
 * @param animationDuration The duration of the progress indicator's animation.
 * @param animationDelay The delay before the progress indicator's animation starts.
 * @param backgroundColor The color of the progress indicator's background.
 * @param onProgressColorChange The callback to invoke when the progress color changes.
 * @param progressValueTextStyle The style to apply to the progress value text.
 * @param progressValuesTextColor The color of the progress value text.
 */
@Composable
fun CustomCircularProgressIndicator(
    label: String,
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 8.dp,
    animationDuration: Int = 1000,
    animationDelay: Int = 100,
    backgroundColor: Color = Color.LightGray,
    onProgressColorChange: (Color) -> Unit = {},
    progressValueTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    progressValuesTextColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var currentProgress by remember { mutableStateOf(0f) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

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
            onProgressColorChange(color)
            Canvas(
                modifier = Modifier
                    .size(size)
                    .hoverable(interactionSource = interactionSource)
            ) {
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
        if (hovered) Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = progressValueTextStyle,
                color = progressValuesTextColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}