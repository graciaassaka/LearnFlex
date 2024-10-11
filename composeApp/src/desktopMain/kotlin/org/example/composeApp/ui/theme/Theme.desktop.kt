package org.example.composeApp.ui.theme

import androidx.compose.runtime.Composable

/**
 * Retrieves the platform-specific color scheme based on the provided theme settings.
 *
 * @param darkTheme A boolean indicating whether the dark theme should be used.
 * @param dynamicColor A boolean indicating whether dynamic color should be applied.
 * @return A ColorScheme instance appropriate for the given theme settings.
 */
@Composable
actual fun getPlatformColorScheme(darkTheme: Boolean, dynamicColor: Boolean) = if (darkTheme) darkScheme else lightScheme
