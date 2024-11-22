package org.example.composeApp.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import org.example.composeApp.component.SpeakingBird
import org.example.composeApp.util.Orientation

/**
 * A composable function that creates a profile creation layout content with a responsive design,
 * showing different components based on the orientation.
 *
 * @param orientation The orientation of the layout (Horizontal or Vertical).
 * @param enabled A boolean flag indicating whether the components within the layout are enabled.
 * @param caption The question to be displayed by the speaking bird.
 * @param modifier A [Modifier] to be applied to the root layout.
 * @param content A composable content lambda that displays the main content inside the layout.
 */
@Composable
fun CreateProfileLayout(
    orientation: Orientation,
    enabled: Boolean,
    caption: String,
    modifier: Modifier,
    content: @Composable (ColumnScope.(Int) -> Unit)
) {
    Box(modifier = modifier.fillMaxSize()) {
        SubcomposeLayout(modifier = Modifier.fillMaxSize()) { constraints ->
            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
            val width = constraints.maxWidth
            val height = constraints.maxHeight

            val birdAreaWidth = (width / 2)
            val cardAreaWidth = (width / 2)

            val birdPlaceable = subcompose("bird") {
                SpeakingBird(
                    orientation = orientation,
                    enabled = enabled,
                    modifier = Modifier.width(birdAreaWidth.dp),
                    content = { Text(caption) }
                )
            }.map { it.measure(looseConstraints) }

            val contentPlaceable = subcompose("content") {
                Column(content = { content(cardAreaWidth) })
            }.map {
                when (orientation) {
                    Orientation.Horizontal -> it.measure(
                        constraints.copy(minWidth = cardAreaWidth, maxWidth = cardAreaWidth)
                    )

                    Orientation.Vertical   -> it.measure(constraints)
                }
            }

            layout(width, height) {
                when (orientation) {
                    Orientation.Horizontal -> {
                        val birdX = (birdAreaWidth - (birdPlaceable.firstOrNull()?.width ?: 0)) / 2
                        birdPlaceable.forEach { it.place(x = birdX, y = (height - it.height) / 2) }
                        contentPlaceable.forEach { it.place(x = birdAreaWidth, y = 0) }
                    }

                    Orientation.Vertical   -> {
                        val birdHeight = birdPlaceable.firstOrNull()?.height ?: 0
                        birdPlaceable.forEach { it.place(x = 0, y = 100) }
                        contentPlaceable.forEach { it.place(x = 0, y = birdHeight) }
                    }
                }
            }
        }
    }
}