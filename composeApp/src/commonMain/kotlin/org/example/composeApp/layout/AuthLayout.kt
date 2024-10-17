package org.example.composeApp.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import org.example.composeApp.component.AppBanner
import org.example.composeApp.component.CustomSnackbar
import org.example.composeApp.component.auth.AnimatedAuthCard
import org.example.composeApp.component.auth.AnimatedBottomSheet
import org.example.composeApp.util.Orientation
import org.example.shared.util.SnackbarType

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
    isVisible: Boolean,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)
)
{
    val orientation = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) Orientation.Vertical else Orientation.Horizontal

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) { CustomSnackbar(it, snackbarType) } }) {
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
                    when (orientation)
                    {
                        Orientation.Horizontal -> AnimatedAuthCard(
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
                }.map {
                    when (orientation)
                    {
                        Orientation.Horizontal -> it.measure(constraints.copy(minWidth = cardWidth, maxWidth = cardWidth))
                        Orientation.Vertical -> it.measure(constraints)
                    }
                }

                layout(width, height) {
                    when (orientation)
                    {
                        Orientation.Horizontal ->
                        {
                            val logoWidth = appBannerPlaceable.firstOrNull()?.width ?: 0
                            val logoX = (logoAreaWidth - logoWidth) / 2
                            appBannerPlaceable.forEach { it.place(x = logoX, y = (height - it.height) / 2) }
                            contentPlaceable.forEach { it.place(x = logoAreaWidth, y = 0) }
                        }

                        Orientation.Vertical ->
                        {
                            val appBannerY = 200
                            appBannerPlaceable.forEach { it.place(x = (width - it.width) / 2, y = appBannerY) }
                            val contentY = 500
                            contentPlaceable.forEach { it.place(x = (width - it.width) / 2, y = contentY) }
                        }
                    }
                }
            }
        }
    }
}