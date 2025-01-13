package org.example.composeApp.presentation.state

import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.util.BundleManager
import org.example.shared.domain.model.util.SessionManager

/**
 * Represents the state of the application.
 *
 * @property profile The user's profile information.
 * @property bundleManager Manages the application's bundles.
 * @property sessionManager Manages the user's session.
 * @property isLoading Indicates if the application is currently loading.
 * @property error Holds any error that occurs in the application.
 */
data class AppState(
    val profile: Profile? = null,
    val bundleManager: BundleManager = BundleManager(emptyList()),
    val sessionManager: SessionManager = SessionManager(emptyList()),
    val isLoading: Boolean = false,
    val error: Throwable? = null
)
