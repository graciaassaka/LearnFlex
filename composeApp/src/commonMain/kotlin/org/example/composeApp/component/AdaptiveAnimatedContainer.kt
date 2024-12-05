package org.example.composeApp.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.composeApp.component.auth.AnimatedBottomSheet
import org.example.composeApp.util.Orientation

/**
 * A composable container that adapts its animation based on the given orientation.
 *
 * @param orientation The orientation of the container (Horizontal or Vertical).
 * @param isVisible A boolean indicating whether the container is visible.
 * @param onAnimationFinished A callback function to be invoked when the animation finishes.
 * @param cardWidth The width of the card in dp.
 * @param content The composable content to be displayed inside the container.
 */
@Composable
fun AdaptiveAnimatedContainer(
    orientation: Orientation,
    isVisible: Boolean,
    onAnimationFinished: () -> Unit,
    cardWidth: Int,
    content: @Composable (ColumnScope.() -> Unit)
) = when (orientation) {
    Orientation.Horizontal -> AnimatedCard(
        isVisible = isVisible,
        onAnimationFinished = onAnimationFinished,
        modifier = Modifier.width(cardWidth.dp).fillMaxHeight(),
        content = content
    )

    Orientation.Vertical -> AnimatedBottomSheet(
        isVisible = isVisible,
        onAnimationFinished = onAnimationFinished,
        content = content
    )
}



