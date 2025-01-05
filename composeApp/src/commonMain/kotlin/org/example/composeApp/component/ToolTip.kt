package org.example.composeApp.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Displays a tooltip with the specified text alongside content.
 *
 * @param text The text to be displayed in the tooltip.
 * @param modifier The modifier to be applied to the tooltip.
 * @param content The composable content to which the tooltip will be attached.
 */
@Composable
expect fun ToolTip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)