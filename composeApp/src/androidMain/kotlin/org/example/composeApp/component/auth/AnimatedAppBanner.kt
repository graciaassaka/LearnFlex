package org.example.composeApp.component.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.example.learnflex.R

/**
 * Displays an animated banner with the app's logo and name. The banner
 * slides in and out horizontally based on the visibility state.
 *
 * @param isVisible Determines the visibility state of the banner.
 * @param modifier Modifier to be applied to the banner layout.
 */
@Composable
fun AnimatedAppBanner(
    isVisible: Boolean,
    modifier: Modifier = Modifier
)
{
    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(isVisible) {
        transitionState.targetState = isVisible
    }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = slideInHorizontally(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) { -it },
        exit = slideOutHorizontally(tween(durationMillis = 250, easing = FastOutLinearInEasing)) { it },
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(id = R.dimen.logo_size))
            )
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun AnimatedAppBannerPreview()
{
    AnimatedAppBanner(isVisible = true)
}