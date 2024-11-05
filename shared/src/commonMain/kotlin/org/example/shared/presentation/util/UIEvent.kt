package org.example.shared.presentation.util

import org.example.shared.presentation.navigation.Route

/**
 * Sealed class representing the type of a Snackbar.
 */
sealed class UIEvent {
    data class ShowSnackbar(val message: String, val type: SnackbarType) : UIEvent()
    data class Navigate(val destination: Route) : UIEvent()
}