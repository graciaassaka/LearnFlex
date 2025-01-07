package org.example.composeApp.presentation.ui.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import org.example.composeApp.presentation.ui.component.AnimatedProgressBar
import org.example.composeApp.presentation.ui.dimension.Padding

/**
 * A composable function that aligns labeled bars, ensuring that the labels and bars are
 * properly aligned side by side. The labels and bars are provided as separate lists,
 * and both lists must have the same number of elements.
 *
 * @param labels A list of strings representing the labels that will be displayed next to the bars.
 * @param bars A list of floats representing the progress values for each bar, where each float is between 0.0 and 1.0.
 * @param modifier The modifier to be applied to the composable layout, which can be used to customize
 *        the appearance and behavior of the layout.
 */
@Composable
fun AlignedLabeledBarsLayout(
    labels: List<String>,
    bars: List<Float>,
    modifier: Modifier = Modifier
) {
    require(labels.size == bars.size) { "The number of labels must be equal to the number of bars." }
    SubcomposeLayout(modifier) { constraints ->
        // First measure text to get their widths
        val labelPlaceables = subcompose("labels") {
            labels.forEach {
                Text(
                    text = it,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Start
                )
            }
        }.map { it.measure(Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )) }

        val maxLabelWidth = labelPlaceables.maxOf { it.width }
        val horizontalSpacing = 8.dp.roundToPx()

        // Ensure we leave at least 70% of the width for bars
        val minBarWidth = (constraints.maxWidth * 0.7f).toInt()
        val availableBarWidth = maxOf(
            minBarWidth,
            constraints.maxWidth - maxLabelWidth - horizontalSpacing
        )

        // Measure bars
        val barPlaceables = subcompose("bars") {
            bars.forEach {
                AnimatedProgressBar(
                    progress = it,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }.map { measurable ->
            measurable.measure(Constraints(
                minWidth = minBarWidth,
                maxWidth = availableBarWidth,
                minHeight = 0,
                maxHeight = constraints.maxHeight
            ))
        }

        val height = labelPlaceables.indices.sumOf { i ->
            maxOf(labelPlaceables[i].height, barPlaceables[i].height)
        } + (labelPlaceables.size - 1) * Padding.SMALL.dp.roundToPx()

        layout(constraints.maxWidth, height) {
            var y = 0
            repeat(labelPlaceables.size) { i ->
                val labelPlaceable = labelPlaceables[i]
                val barPlaceable = barPlaceables[i]

                // Place label on the left
                labelPlaceable.place(0, y)

                // Place bar with proper width
                barPlaceable.place(
                    x = maxLabelWidth + horizontalSpacing,
                    y = y + (labelPlaceable.height - barPlaceable.height) / 2
                )

                y += maxOf(labelPlaceable.height, barPlaceable.height) +
                        Padding.SMALL.dp.roundToPx()
            }
        }
    }
}
