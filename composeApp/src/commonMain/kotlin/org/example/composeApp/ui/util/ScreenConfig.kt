package org.example.composeApp.ui.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import org.example.shared.presentation.util.SnackbarType

/**
 * Data class representing the configuration of a screen.
 *
 * @param UIState The state of the UI.
 */
data class ScreenConfig<UIState>(
    val windowSizeClass: WindowSizeClass,
    val snackbarHostState: SnackbarHostState,
    val snackbarType: MutableState<SnackbarType>,
    val uiState: State<UIState>,
    val isScreenVisible: State<Boolean>,
)
