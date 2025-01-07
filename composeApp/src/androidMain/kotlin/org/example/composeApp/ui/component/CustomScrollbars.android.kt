package org.example.composeApp.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CustomVerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier
) {
    // no-op
}

@Composable
actual fun CustomVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier
) {
    // no-op
}

@Composable
actual fun CustomVerticalScrollbar(
    lazyGridState: LazyGridState,
    modifier: Modifier
) {
    // no-op
}