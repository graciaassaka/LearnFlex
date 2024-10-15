package org.example.shared.presentation.util

import org.example.shared.presentation.navigation.Route

sealed class UIEvent
{
    data class ShowSnackbar(val message: String, val type: SnackbarType) : UIEvent()

    data class Navigate(val destination: Route) : UIEvent()
}