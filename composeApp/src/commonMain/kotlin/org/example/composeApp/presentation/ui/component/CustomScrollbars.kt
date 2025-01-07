package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A custom vertical scrollbar for a [LazyList] that displays the current scroll position.
 *
 * @param lazyListState The state of the [LazyList] to which the scrollbar is attached.
 * @param modifier The modifier to apply to the scrollbar.
 */
@Composable
expect fun CustomVerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
)

/**
 * A custom vertical scrollbar for a [Scrollable] that displays the current scroll position.
 *
 * @param scrollState The state of the [Scrollable] to which the scrollbar is attached.
 * @param modifier The modifier to apply to the scrollbar.
 */
@Composable
expect fun CustomVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
)

/**
 * A custom vertical scrollbar for a [LazyGrid] that displays the current scroll position.
 *
 * @param lazyGridState The state of the [LazyGrid] to which the scrollbar is attached.
 * @param modifier The modifier to apply to the scrollbar.
 */
@Composable
expect fun CustomVerticalScrollbar(
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier
)