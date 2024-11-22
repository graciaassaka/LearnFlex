package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthClient

/**
 * Use case for sending a password reset email.
 *
 * @property authClient The authentication service used to send the password reset email.
 */
class SendPasswordResetEmailUseCase(private val authClient: AuthClient) {

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the password reset email to.
     */
    suspend operator fun invoke(email: String) = authClient.sendPasswordResetEmail(email)
}