package org.example.composeApp.component.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import org.example.composeApp.component.GradientBackground

/**
 * Displays an animated card for authentication with slide-in/slide-out transitions and gradient background.
 *
 * @param isVisible Determines if the card is visible or not.
 * @param onAnimationFinished Callback invoked when the animation is finished.
 * @param modifier Modifier to be applied to the card.
 * @param content Composable content to be displayed within the card.
 */
@Composable
fun AnimatedAuthCard(
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
        enter = slideInHorizontally(tween(durationMillis = 500, easing = LinearOutSlowInEasing)) { it },
        exit = slideOutHorizontally(tween(durationMillis = 500, easing = LinearOutSlowInEasing)) { it },
        modifier = modifier,
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                GradientBackground(
                    color1 = MaterialTheme.colorScheme.inversePrimary,
                    color2 = MaterialTheme.colorScheme.surfaceContainerLowest,
                )
                Column(
                    modifier = modifier.fillMaxSize(),
                    content = content
                )
            }
        }
    }
}