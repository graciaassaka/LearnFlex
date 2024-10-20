package org.example.composeApp.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * A composable function that displays an image with a pulsing animation effect.
 *
 * @param image The drawable resource to be used as the image.
 * @param modifier The modifier to be applied to this composable. Defaults to an empty modifier.
 * @param size The size of the image in density-independent pixels (dp). Defaults to 100 dp.
 */
@Composable
fun PulsingImage(
    image: DrawableResource,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
)
{
    var scale by remember { mutableStateOf(1f) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        scale = 1.2f
        delay(1000)
        scale = .8f
        delay(1000)
    }

    Image(
        painter = painterResource(image),
        contentDescription = null,
        modifier = modifier
            .scale(animatedScale)
            .size(size)
    )
}