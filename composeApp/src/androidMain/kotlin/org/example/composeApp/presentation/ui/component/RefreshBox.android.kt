package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun RefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        contentAlignment = Alignment.TopCenter,
        content = content
    )
}