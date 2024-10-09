package org.example.composeApp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getPlatformColorScheme(darkTheme: Boolean, dynamicColor: Boolean) = with(LocalContext.current) {
   when{
         dynamicColor -> if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
         darkTheme -> darkScheme
         else -> lightScheme
   }
}
