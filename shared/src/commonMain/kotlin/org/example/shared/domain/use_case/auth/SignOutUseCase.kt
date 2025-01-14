package org.example.shared.domain.use_case.auth

import org.example.shared.domain.client.AuthClient

/**
 * Use case for signing out a user.
 *
 * @property authClient The authentication service used to handle the sign-out operation.
 */
class SignOutUseCase(private val authClient: AuthClient) {
    /**
     * Invokes the use case to sign out the currently signed-in user.
     *
     * This suspension function delegates the sign-out operation to the underlying
     * authentication client.
     */
    suspend operator fun invoke() = authClient.signOut()
}