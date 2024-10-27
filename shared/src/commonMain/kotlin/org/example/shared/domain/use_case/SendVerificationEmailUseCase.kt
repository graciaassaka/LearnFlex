package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthService

/**
 * Use case for sending a verification email.
 *
 * @property authService The authentication service used to send the verification email.
 */
class SendVerificationEmailUseCase(private val authService: AuthService) {

    /**
     * Invokes the use case to send a verification email.
     */
    suspend operator fun invoke() = authService.sendEmailVerification()
}