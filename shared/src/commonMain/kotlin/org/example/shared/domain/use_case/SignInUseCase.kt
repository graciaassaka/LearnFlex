package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthService

/**
 * Use case class for signing in a user.
 *
 * @property authService The authentication service used to sign in the user.
 */
class SignInUseCase(private val authService: AuthService)
{
    /**
     * Invokes the use case to sign in a user with the provided email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @return The result of the sign-in operation.
     */
    suspend operator fun invoke(email: String, password: String) = authService.signIn(email, password)
}