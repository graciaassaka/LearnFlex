package org.example.composeApp.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

/**
 * A custom scrollbar for a LazyGrid.
 *
 * @param gridState The state of the LazyGrid.
 * @param modifier Modifier to be applied to the scrollbar.
 */
@Composable
fun CustomLazyGridScrollbar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier
) {
    val targetAlpha = if (gridState.isScrollInProgress) 1f else 0.3f
    val duration = if (gridState.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "scrollbar"
    )

    val scrollbarColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .padding(end = 4.dp)
            .width(8.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
        ) {
            // Get the number of items before and after the visible items
            val beforeVisible = gridState.firstVisibleItemIndex
            val visible = gridState.layoutInfo.visibleItemsInfo.size
            val afterVisible = gridState.layoutInfo.totalItemsCount - (beforeVisible + visible)
            val totalItems = beforeVisible + visible + afterVisible

            // Calculate the relative position (0f to 1f)
            val scrollPosition = beforeVisible.toFloat() / (totalItems - visible).coerceAtLeast(1)

            // Calculate scrollbar size relative to the total size
            val scrollbarSize = (visible.toFloat() / totalItems.coerceAtLeast(1)) * size.height
                .coerceAtLeast(40f) // Minimum scrollbar size
                .coerceAtMost(size.height * 0.8f) // Maximum scrollbar size

            // Calculate scrollbar position
            val maxScrollPosition = size.height - scrollbarSize
            val scrollbarOffset = maxScrollPosition * scrollPosition

            drawRoundRect(
                color = scrollbarColor,
                topLeft = Offset(0f, scrollbarOffset),
                size = Size(size.width, scrollbarSize),
                cornerRadius = CornerRadius(size.width / 2)
            )
        }
    }
}