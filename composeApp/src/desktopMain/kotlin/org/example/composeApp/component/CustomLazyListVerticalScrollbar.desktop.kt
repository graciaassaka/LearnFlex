package org.example.composeApp.component

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Suppress("OVERLOAD_RESOLUTION_AMBIGUITY")
@Composable
actual fun CustomLazyListVerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState = lazyListState),
        modifier = modifier
    )
}