package org.example.composeApp.layout

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import org.example.composeApp.component.getProgressColor
import org.example.composeApp.dimension.Dimension

/**
 * A composable to display a vertical bar graph with color transitions based on progress.
 *
 * @param data The list of data pairs with labels and values.
 * @param modifier Modifier to be applied to the vertical bar graph.
 * @param animationDuration The duration of the progress bar animation in milliseconds.
 * @param animationDelay The delay before the animation starts in milliseconds.
 * @param onBarClicked The callback when a bar is clicked.
 */
@Composable
fun VerticalBarGraph(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000,
    animationDelay: Int = 100,
    onBarClicked: (String) -> Unit = {}
) {
    val maxValue = data.maxOf { it.second }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val barWidth = maxWidth / data.size
        val maxBarHeight = maxHeight * 0.9f

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            data.forEachIndexed { i, (label, value) ->
                var progress by remember(i) { mutableStateOf(0f) }

                LaunchedEffect(value) {
                    progress = value
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        delayMillis = animationDelay,
                        easing = FastOutLinearInEasing
                    ),
                    label = "${label}ProgressFloatAnimation"
                )

                val normalizedProgress = animatedProgress / maxValue

                val progressColor = remember(normalizedProgress) {
                    getProgressColor(
                        animatedProgress = normalizedProgress,
                        colors = listOf(
                            0.3f to Color.Red,
                            0.6f to Color.Yellow,
                            1.0f to Color.Green
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .width(barWidth)
                        .height(this@BoxWithConstraints.maxHeight)
                ) {
                    Box(
                        modifier = Modifier
                            .width(barWidth * 0.75f)
                            .height(maxBarHeight * (animatedProgress / maxValue))
                            .background(
                                color = progressColor,
                                shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp)
                            )
                            .clickable { onBarClicked(label) }
                    )

                    Text(
                        text = label,
                        modifier = Modifier.width(barWidth),
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}