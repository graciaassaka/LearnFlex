package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthClient

/**
 * Use case for sending a verification email.
 *
 * @property authClient The authentication service used to send the verification email.
 */
class SendVerificationEmailUseCase(private val authClient: AuthClient) {

    /**
     * Invokes the use case to send a verification email.
     */
    suspend operator fun invoke() = authClient.sendEmailVerification()
}