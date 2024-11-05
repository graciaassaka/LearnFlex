package org.example.composeApp.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import org.example.composeApp.component.AdaptiveAnimatedContainer
import org.example.composeApp.component.CustomSnackbar
import org.example.composeApp.component.SpeakingBird
import org.example.composeApp.util.Orientation
import org.example.shared.presentation.util.SnackbarType

/**
 * A composable function that creates a profile creation layout with a responsive design,
 * showing different components based on the window size class and orientation.
 *
 * @param windowSizeClass The dimension classes for width and height of the window.
 * @param snackbarHostState The state of the Snackbar host to display Snackbar messages.
 * @param snackbarType The type of the Snackbar (e.g., success, error, warning, info).
 * @param title The title to be displayed by the speaking bird.
 * @param isLoading Determines if the components are loading or not.
 * @param isVisible Determines if the components are visible or not with an animation.
 * @param onAnimationFinished A callback invoked when the visibility animation is finished.
 * @param modifier A [Modifier] to be applied to the root layout.
 * @param content A composable content lambda that displays the main content inside the layout.
 */
@Composable
fun CreateProfileLayout(
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    title: String,
    isLoading: Boolean,
    isVisible: Boolean,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)
) {
    val orientation =
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) Orientation.Vertical else Orientation.Horizontal

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { CustomSnackbar(it, snackbarType) } },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                        isLoading = isLoading,
                        modifier = Modifier.width(birdAreaWidth.dp),
                        content = { Text(title) }
                    )
                }.map { it.measure(looseConstraints) }

                val contentPlaceable = subcompose("content") {
                    AdaptiveAnimatedContainer(
                        orientation = orientation,
                        isVisible = isVisible,
                        onAnimationFinished = onAnimationFinished,
                        cardWidth = cardAreaWidth,
                        content = content
                    )
                }.map {
                    when (orientation) {
                        Orientation.Horizontal -> it.measure(
                            constraints.copy(
                                minWidth = cardAreaWidth,
                                maxWidth = cardAreaWidth
                            )
                        )

                        Orientation.Vertical -> it.measure(constraints)
                    }
                }

                layout(width, height) {
                    when (orientation) {
                        Orientation.Horizontal -> {
                            val birdX = (birdAreaWidth - (birdPlaceable.firstOrNull()?.width ?: 0)) / 2
                            birdPlaceable.forEach { it.place(x = birdX, y = (height - it.height) / 2) }
                            contentPlaceable.forEach { it.place(x = birdAreaWidth, y = 0) }
                        }

                        Orientation.Vertical -> {
                            val birdHeight = birdPlaceable.firstOrNull()?.height ?: 0
                            birdPlaceable.forEach { it.place(x = 0, y = 100) }
                            contentPlaceable.forEach { it.place(x = 0, y = birdHeight) }
                        }
                    }
                }
            }
        }
    }
}
