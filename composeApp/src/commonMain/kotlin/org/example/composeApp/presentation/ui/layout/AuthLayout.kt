package org.example.composeApp.presentation.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import org.example.composeApp.presentation.ui.component.AdaptiveAnimatedContainer
import org.example.composeApp.presentation.ui.component.AppBanner
import org.example.composeApp.presentation.ui.component.CustomSnackbar
import org.example.composeApp.presentation.ui.constant.Orientation
import org.example.composeApp.presentation.ui.util.SnackbarType

/**
 * A composable function that creates an authentication layout with a responsive design,
 * showing different components based on the window size class and orientation.
 *
 * @param windowSizeClass The dimension classes for width and height of the window.
 * @param snackbarHostState The state of the Snackbar host to display Snackbar messages.
 * @param snackbarType The type of the Snackbar (e.g., success, error, warning, info).
 * @param isVisible Determines if the components are visible or not with an animation.
 * @param onAnimationFinished A callback invoked when the visibility animation is finished.
 * @param modifier A [Modifier] to be applied to the root layout.
 * @param content A composable content lambda that displays the main content inside the layout.
 */
@Composable
fun AuthLayout(
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    enabled: Boolean,
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
            SubcomposeLayout(
                modifier = Modifier.fillMaxSize()
            ) { constraints ->
                val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                val width = constraints.maxWidth
                val height = constraints.maxHeight

                val logoAreaWidth = (width / 3)
                val cardWidth = (width * 2 / 3)

                val appBannerPlaceable = subcompose("appName") {
                    AppBanner(
                        orientation = orientation,
                        isVisible = isVisible,
                        modifier = Modifier.width(logoAreaWidth.dp)
                    )
                }.map { it.measure(looseConstraints) }

                val contentPlaceable = subcompose("content") {
                    AdaptiveAnimatedContainer(
                        orientation = orientation,
                        isVisible = isVisible,
                        onAnimationFinished = onAnimationFinished,
                        cardWidth = cardWidth,
                        content = content
                    )
                }.map {
                    when (orientation) {
                        Orientation.Horizontal -> it.measure(
                            constraints.copy(
                                minWidth = cardWidth,
                                maxWidth = cardWidth
                            )
                        )

                        Orientation.Vertical   -> it.measure(constraints)
                    }
                }

                layout(width, height) {
                    when (orientation) {
                        Orientation.Horizontal -> {
                            val logoX = (logoAreaWidth - (appBannerPlaceable.firstOrNull()?.width ?: 0)) / 2
                            appBannerPlaceable.forEach { it.place(x = logoX, y = (height - it.height) / 2) }
                            contentPlaceable.forEach { it.place(x = logoAreaWidth, y = 0) }
                        }

                        Orientation.Vertical   -> {
                            val appBannerY = 200
                            appBannerPlaceable.forEach { it.place(x = (width - it.width) / 2, y = appBannerY) }
                            val contentY = 500
                            contentPlaceable.forEach { it.place(x = (width - it.width) / 2, y = contentY) }
                        }
                    }
                }
            }
            if (!enabled) CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}