package org.example.shared.presentation.state

import org.example.shared.domain.model.User

/**
 * Data class representing the shared state of the application.
 *
 * @property userData The user data, if available.
 * @property isLoading Whether a loading indicator should be displayed or not.
 * @property error The error that occurred, if any.
 */
data class SharedState(
    val userData: User? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)