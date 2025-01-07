package org.example.composeApp.ui.component.create_profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.composeApp.ui.component.AnimatedCard
import org.example.composeApp.ui.component.CustomSnackbar
import org.example.composeApp.ui.dimension.Dimension
import org.example.composeApp.ui.dimension.Padding
import org.example.composeApp.ui.layout.CreateProfileLayout
import org.example.composeApp.ui.util.Orientation
import org.example.shared.presentation.util.SnackbarType

/**
 * A composable function that creates a style questionnaire layout with a responsive design,
 * showing different components based on the window size class and orientation.
 *
 * @param windowSizeClass The dimension classes for width and height of the window.
 * @param snackbarHostState The state of the Snackbar host to display Snackbar messages.
 * @param snackbarType The type of the Snackbar (e.g., success, error, warning, info).
 * @param question The question to be displayed by the speaking bird.
 * @param enabled A boolean flag indicating whether the components within the layout are enabled.
 * @param isVisible Determines if the components are visible or not with an animation.
 * @param onAnimationFinished A callback invoked when the visibility animation is finished.
 * @param modifier A [Modifier] to be applied to the root layout.
 * @param content A composable content lambda that displays the main content inside the layout.
 */
@Composable
fun StyleQuestionnaireForm(
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    question: String,
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
            enabled = enabled && question.isNotBlank(),
            caption = question,
            modifier = modifier,
        ) { cardAreaWidth ->
            AnimatedCard(
                isVisible = isVisible,
                onAnimationFinished = onAnimationFinished,
                modifier = Modifier
                    .width(cardAreaWidth.dp)
                    .styleCardHeight(orientation)
                    .padding(Padding.MEDIUM.dp),
                content = content
            )
        }
    }
}

private fun Modifier.styleCardHeight(orientation: Orientation) = when (orientation) {
    Orientation.Horizontal -> fillMaxHeight()
    Orientation.Vertical   -> height(Dimension.STYLE_QUESTIONNAIRE_CARD_HEIGHT.dp)
}

