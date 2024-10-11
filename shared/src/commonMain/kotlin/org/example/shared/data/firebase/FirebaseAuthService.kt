package org.example.shared.data.firebase

import org.example.shared.domain.service.AuthService

/**
 * Expects a platform-specific implementation of FirebaseAuthService.
 * This class should extend AuthService and provide authentication functionalities.
 */
expect class FirebaseAuthService : AuthService