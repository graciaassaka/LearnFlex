package org.example.composeApp.util

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A static composition local for storing the maximum file size.
 */
object LocalComposition {
    val MaxFileSize = staticCompositionLocalOf<Long> { error("No max file size provided") }
}