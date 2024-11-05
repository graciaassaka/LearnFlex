package org.example.composeApp.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.app_name
import learnflex.composeapp.generated.resources.ic_logo
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.util.Orientation
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Displays an animated app banner with a logo and application name, which can slide in and out horizontally.
 *
 * @param orientation The orientation of the banner, either vertical or horizontal.
 * @param isVisible Determines if the banner is visible or not.
 * @param modifier A modifier to be applied to the banner layout.
 */
@Composable
fun AppBanner(
    orientation: Orientation,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(isVisible) {
        transitionState.targetState = isVisible
    }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = slideInHorizontally(tween(durationMillis = 500, easing = LinearOutSlowInEasing)) { -it },
        exit = slideOutHorizontally(tween(durationMillis = 500, easing = FastOutLinearInEasing)) {
            if (orientation == Orientation.Horizontal) -it else it
        },
    ) {
        ComponentLayout(orientation = orientation, modifier = modifier) {
            Image(
                painter = painterResource(Res.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(
                    if (orientation == Orientation.Vertical) Dimension.LOGO_SIZE_MEDIUM.dp else Dimension.LOGO_SIZE_LARGE.dp
                )
            )
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ComponentLayout(
    orientation: Orientation,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = when (orientation) {
    Orientation.Vertical -> Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = { content() }
    )

    Orientation.Horizontal -> Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = { content() }
    )
}