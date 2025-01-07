package org.example.composeApp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import org.example.learnflex.R

/**
 * Retrieves the platform-specific color scheme based on the provided theme settings.
 *
 * @param darkTheme Boolean indicating if the dark theme is enabled.
 * @param dynamicColor Boolean indicating if dynamic color is enabled.
 * @return The appropriate color scheme based on the theme settings.
 */
@Composable
actual fun getPlatformColorScheme(darkTheme: Boolean, dynamicColor: Boolean) = with(LocalContext.current) {
    when {
        dynamicColor -> if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
        darkTheme -> darkScheme
        else -> lightScheme
    }
}

/**
 * Retrieves the platform-specific shape configuration for the UI components.
 *
 * @return A Shapes instance containing the shape definitions appropriate for the platform.
 */
@Composable
actual fun getPlatformShape(): Shapes = Shapes(
    small = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
    medium = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_medium)),
    large = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_large))
)