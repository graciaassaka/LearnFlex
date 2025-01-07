package org.example.composeApp.ui.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A composable function that provides a refreshable container with a pull-to-refresh style functionality.
 *
 * @param isRefreshing Indicates whether the content is currently being refreshed.
 * @param onRefresh Callback invoked when the user triggers a refresh action.
 * @param modifier Modifier to be applied to the container.
 * @param content The content to be displayed within the refreshable container.
 */
@Composable
expect fun RefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
)