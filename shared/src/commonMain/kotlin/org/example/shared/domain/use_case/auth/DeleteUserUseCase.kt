package org.example.shared.domain.use_case.auth

import org.example.shared.domain.client.AuthClient

/**
 * Use case for deleting a user.
 *
 * @property authClient The authentication service used to delete the user.
 */
class DeleteUserUseCase(private val authClient: AuthClient) {

    /**
     * Invokes the use case to delete a user.
     *
     * @return A coroutine that deletes the user.
     */
    suspend operator fun invoke() = authClient.deleteUser()
}