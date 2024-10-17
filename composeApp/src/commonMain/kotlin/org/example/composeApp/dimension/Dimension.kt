@file:Suppress("unused")

package org.example.composeApp.dimension

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Enum class representing various dimensions used throughout the application.
 *
 * Each dimension is associated with a Dp value, which provides a consistent unit of measurement
 * for UI elements such as logos, corner radii, and button heights.
 */
enum class Dimension(val dp: Dp) {
    LOGO_SIZE_MEDIUM(80.dp),
    LOGO_SIZE_LARGE(160.dp),
    CORNER_RADIUS_SMALL(8.dp),
    CORNER_RADIUS_MEDIUM(16.dp),
    CORNER_RADIUS_LARGE(32.dp),
    AUTH_BUTTON_HEIGHT(54.dp)
}

/**
 * Enum class representing different padding sizes for UI elements in application.
 *
 * @property dp The padding size in density-independent pixels (dp).
 */
enum class Padding(val dp: Dp) {
    SMALL(8.dp),
    MEDIUM(16.dp),
    LARGE(32.dp),
    XLARGE(64.dp)
}

/**
 * Enum class representing different spacing sizes in density-independent pixels (dp).
 *
 * @property dp The spacing size in dp.
 */
enum class Spacing(val dp: Dp) {
    SMALL(8.dp),
    MEDIUM(16.dp),
    LARGE(32.dp),
    XLARGE(64.dp)
}