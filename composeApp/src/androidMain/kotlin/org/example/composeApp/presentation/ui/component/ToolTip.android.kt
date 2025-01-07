package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ToolTip(
    text: String,
    modifier: Modifier,
    content: @Composable (() -> Unit)
) = Box(
    modifier = modifier,
    content = { content() }
)
