package org.example.shared.domain.use_case.auth

import org.example.shared.domain.client.AuthClient

/**
 * Use case class for signing up a user.
 *
 * @property authClient The authentication service used to sign up the user.
 */
class SignUpUseCase(private val authClient: AuthClient) {
    /**
     * Invokes the use case to sign up a user with the provided email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @return The result of the sign-up operation.
     */
    suspend operator fun invoke(email: String, password: String) = authClient.signUp(email, password)
}