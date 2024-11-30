package org.example.composeApp.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A custom vertical scrollbar for a [LazyList] that displays the current scroll position.
 *
 * @param lazyListState The state of the [LazyList] to which the scrollbar is attached.
 * @param modifier The modifier to apply to the scrollbar.
 */
@Composable
expect fun CustomLazyListVerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
)