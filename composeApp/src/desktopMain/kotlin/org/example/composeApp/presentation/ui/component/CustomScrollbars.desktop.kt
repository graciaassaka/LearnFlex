package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CustomVerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState = lazyListState),
        modifier = modifier
    )
}

@Composable
actual fun CustomVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState = scrollState),
        modifier = modifier
    )
}

@Composable
actual fun CustomVerticalScrollbar(
    lazyGridState: LazyGridState,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState = lazyGridState),
        modifier = modifier
    )
}