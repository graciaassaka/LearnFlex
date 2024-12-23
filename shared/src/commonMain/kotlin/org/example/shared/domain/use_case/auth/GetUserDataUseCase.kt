package org.example.shared.domain.use_case.auth

import org.example.shared.domain.client.AuthClient

/**
 * Use case class for fetching user data.
 *
 * @property authClient The authentication service used to fetch user data.
 */
class GetUserDataUseCase(private val authClient: AuthClient) {

    /**
     * Invokes the use case to fetch user data.
     *
     * @return The result of the user data fetch operation.
     */
    suspend operator fun invoke() = authClient.getUserData()
}