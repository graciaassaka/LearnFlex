package org.example.composeApp.presentation.ui.component

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize

/**
 * A box that displays a shimmer effect when loading data.
 *
 * @param isLoading Whether the data is loading.
 * @param height The height of the box.
 * @param width The width of the box.
 * @param modifier The modifier to apply to the box.
 * @param content The content to display when the data is loaded.
 */
@Composable
fun ShimmerBox(
    isLoading: Boolean,
    height: Dp,
    width: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (isLoading) Box(
        modifier = modifier
            .height(height)
            .width(width)
            .shimmerEffect()
    )
    else content()
}

/**
 * A modifier that applies a shimmer effect to a composable.
 */
@Composable
fun Modifier.shimmerEffect() = composed {
    val size = remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.value.width.toFloat(),
        targetValue = 2 * size.value.width.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(1000)),
        label = "shimmerOffsetX",
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                Color.LightGray,
                MaterialTheme.colorScheme.surface,
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.value.width.toFloat(), size.value.height.toFloat()),
        )
    ).onGloballyPositioned { coordinates ->
        size.value = coordinates.size
    }
}