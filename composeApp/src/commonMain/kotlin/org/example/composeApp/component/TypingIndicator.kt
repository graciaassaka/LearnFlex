package org.example.composeApp.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.example.composeApp.dimension.Spacing

/**
 * A composable function that displays a typing indicator with three animated dots.
 *
 * @param modifier The modifier to be applied to the typing indicator.
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    var dotsCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            repeat(3) {
                delay(300)
                dotsCount = (dotsCount % 3).inc()
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.X_SMALL.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { Dot(visible = it < dotsCount) }
    }
}

@Composable
private fun Dot(visible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = alpha))
    )
}