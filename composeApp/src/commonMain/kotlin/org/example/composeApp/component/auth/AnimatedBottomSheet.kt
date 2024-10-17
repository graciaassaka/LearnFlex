package org.example.composeApp.component.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.example.composeApp.component.GradientBackground
import org.example.composeApp.dimension.Dimension

/**
 * A composable function that displays an animated bottom sheet which slides in and out vertically.
 *
 * @param isVisible Determines whether the bottom sheet is visible or not.
 * @param onAnimationFinished Callback invoked when the animation finishes.
 * @param modifier Modifier to be applied to the bottom sheet.
 * @param content Composable content to be displayed within the bottom sheet.
 */
@Composable
fun AnimatedBottomSheet(
    isVisible: Boolean,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)
{
    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(isVisible) {
        transitionState.targetState = isVisible
    }

    LaunchedEffect(transitionState) {
        snapshotFlow { transitionState.isIdle }.collect {
            if (it && !transitionState.currentState && !transitionState.targetState) onAnimationFinished()
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = slideInVertically(tween(durationMillis = 500, easing = LinearOutSlowInEasing)) { it },
        exit = slideOutVertically(tween(durationMillis = 500, easing = LinearOutSlowInEasing)) { it },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = Dimension.CORNER_RADIUS_LARGE.dp,
                        topEnd = Dimension.CORNER_RADIUS_LARGE.dp
                    )
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(
                        topStart = Dimension.CORNER_RADIUS_LARGE.dp,
                        topEnd = Dimension.CORNER_RADIUS_LARGE.dp
                    )
                ),
        ) {
            GradientBackground(
                color1 = MaterialTheme.colorScheme.inversePrimary,
                color2 = MaterialTheme.colorScheme.surfaceContainerLowest
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}

