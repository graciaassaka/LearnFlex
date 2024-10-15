package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthService

/**
 * Use case class for fetching user data.
 *
 * @property authService The authentication service used to fetch user data.
 */
class GetUserDataUseCase(private val authService: AuthService) {

    /**
     * Invokes the use case to fetch user data.
     *
     * @return The result of the user data fetch operation.
     */
    suspend operator fun invoke() = authService.getUserData()
}