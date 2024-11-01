package org.example.shared.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Represents the different routes in the application.
 *
 * This is a sealed class that defines various navigation routes
 * that can be taken within the app. The routes are serialized using
 * the @Serializable annotation.
 */
sealed class Route
{
    @Serializable
    data object Auth : Route()

    @Serializable
    data object CreateProfile : Route()

    @Serializable
    data object Dashboard : Route()
}