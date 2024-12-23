package org.example.shared.data.remote.firebase

import org.example.shared.domain.client.AuthClient

/**
 * Expects a platform-specific implementation of the FirebaseAuthClient.
 * This class should extend the AuthClient interface.
 */
expect class FirebaseAuthClient : AuthClient