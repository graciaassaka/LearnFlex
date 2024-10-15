package org.example.shared.presentation.navigation

import kotlinx.serialization.Serializable

actual sealed class Route
{
    @Serializable
    data object Auth : Route()

    @Serializable
    data object EmailVerification : Route()

    @Serializable
    data object Dashboard : Route()
}