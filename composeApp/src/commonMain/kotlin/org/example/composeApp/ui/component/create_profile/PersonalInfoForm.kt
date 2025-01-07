package org.example.composeApp.ui.component.create_profile

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.composeApp.ui.component.AdaptiveAnimatedContainer
import org.example.composeApp.ui.component.CustomSnackbar
import org.example.composeApp.ui.layout.CreateProfileLayout
import org.example.composeApp.ui.util.Orientation
import org.example.shared.presentation.util.SnackbarType

/**
 * Creates the profile layout with adaptive design based on window size and orientation, and displays
 * a customizable snackbar on a scaffold.
 *
 * @param windowSizeClass Indicates the size of the window to adapt the layout accordingly.
 * @param snackbarHostState State of the snackbar host used in the scaffold.
 * @param snackbarType Type of the snackbar to be displayed (Info, Success, Error, Warning).
 * @param caption The caption text that will be displayed by the content layout.
 * @param enabled Boolean indicating whether the components within the layout are enabled.
 * @param isVisible Boolean indicating whether the animated container within the layout is visible.
 * @param onAnimationFinished Callback function invoked when the animation inside the container finishes.
 * @param modifier Optional [Modifier] to be applied to the root layout.
 * @param content A composable lambda that defines the main content inside the profile layout.
 */
@Composable
fun PersonalInfoForm(
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    caption: String,
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
        CreateProfileLayout(
            orientation = orientation,
            enabled = enabled && caption.isNotBlank(),
            caption = caption,
            modifier = modifier
        ) { cardAreaWidth ->
            AdaptiveAnimatedContainer(
                orientation = orientation,
                isVisible = isVisible,
                onAnimationFinished = onAnimationFinished,
                cardWidth = cardAreaWidth,
                content = content
            )
        }
    }
}
