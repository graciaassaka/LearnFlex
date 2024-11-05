package org.example.shared.presentation.util

/**
 * Sealed class representing the type of a Snackbar.
 */
sealed class SnackbarType {
    data object Info : SnackbarType()
    data object Success : SnackbarType()
    data object Error : SnackbarType()
    data object Warning : SnackbarType()
}