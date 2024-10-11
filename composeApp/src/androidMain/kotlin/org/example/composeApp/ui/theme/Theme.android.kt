package org.example.composeApp.ui.theme

import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Retrieves the platform-specific color scheme based on the provided theme settings.
 *
 * @param darkTheme Boolean indicating if the dark theme is enabled.
 * @param dynamicColor Boolean indicating if dynamic color is enabled.
 * @return The appropriate color scheme based on the theme settings.
 */
@Composable
actual fun getPlatformColorScheme(darkTheme: Boolean, dynamicColor: Boolean) = with(LocalContext.current) {
   when{
         dynamicColor -> if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
         darkTheme -> darkScheme
         else -> lightScheme
   }
}
