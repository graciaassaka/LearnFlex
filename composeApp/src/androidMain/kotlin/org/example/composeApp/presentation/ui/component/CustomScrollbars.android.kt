package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun BoxScope.CustomVerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier
) {
    // no-op
}

@Composable
actual fun BoxScope.CustomVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier
) {
    // no-op
}

@Composable
actual fun BoxScope.CustomVerticalScrollbar(
    lazyGridState: LazyGridState,
    modifier: Modifier
) {
    // no-op
}