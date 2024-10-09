package org.example.composeApp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun getPlatformColorScheme(darkTheme: Boolean, dynamicColor: Boolean) = if (darkTheme) darkScheme else lightScheme
