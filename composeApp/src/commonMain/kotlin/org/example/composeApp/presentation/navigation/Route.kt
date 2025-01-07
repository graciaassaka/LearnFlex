package org.example.composeApp.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Represents the different routes in the application.
 *
 * This is a sealed class that defines various navigation routes
 * that can be taken within the app. The routes are serialized using
 * the @Serializable annotation.
 */
sealed class Route {
    @Serializable
    data object Auth : Route()

    @Serializable
    data object CreateProfile : Route()

    @Serializable
    data object Dashboard : Route()

    @Serializable
    data object Library : Route()

    @Serializable
    data object Study : Route()

    @Serializable
    data object Community : Route()

    @Serializable
    data object Progress : Route()

    @Serializable
    data object Profile : Route()
}