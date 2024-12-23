package org.example.composeApp.layout

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.no_data_available_error
import org.example.composeApp.component.getProgressColor
import org.jetbrains.compose.resources.stringResource

/**
 * A layout that displays a vertical bar graph.
 *
 * @param data The data to display in the bar graph.
 * @param modifier The modifier to apply to the layout.
 * @param animationDuration The duration of the bar graph's animation.
 * @param animationDelay The delay before the bar graph's animation starts.
 * @param onBarClicked The callback to invoke when a bar is clicked.
 */
@Composable
fun VerticalBarGraphLayout(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000,
    animationDelay: Int = 100,
    onBarClicked: (String) -> Unit = {}
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.no_data_available_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

    } else {
        val maxValue = data.maxOf { it.second }

        BoxWithConstraints(modifier = modifier.wrapContentWidth()) {
            val totalWidth = 24.dp * data.size + 12.dp * (data.size - 1)
            val barWidth = totalWidth / data.size
            val maxBarHeight = maxHeight * 0.75f

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.wrapContentSize()
            ) {
                data.forEachIndexed { i, (label, value) ->
                    var progress by remember(i) { mutableStateOf(0f) }
                    val interactionSource = remember(i) { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()
                    var showValue by remember(i) { mutableStateOf(false) }

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
                        if (isHovered || showValue) Text(
                            text = value.toString(),
                            modifier = Modifier.width(barWidth),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(maxBarHeight * (animatedProgress / maxValue))
                                .background(
                                    color = progressColor,
                                    shape = RectangleShape,
                                )
                                .clickable {
                                    onBarClicked(label)
                                    showValue = !showValue
                                }.hoverable(interactionSource = interactionSource)
                        )
                        Text(
                            text = label,
                            modifier = Modifier.width(barWidth),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (i < data.size - 1) Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }
    }
}