package org.example.shared.presentation.util

sealed class SnackbarType
{
    data object Info : SnackbarType()
    data object Success : SnackbarType()
    data object Error : SnackbarType()
    data object Warning : SnackbarType()
}