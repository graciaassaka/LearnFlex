package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthService

/**
 * Use case for deleting a user.
 *
 * @property authService The authentication service used to delete the user.
 */
class DeleteUserUseCase(private val authService: AuthService) {

    /**
     * Invokes the use case to delete a user.
     *
     * @return A coroutine that deletes the user.
     */
    suspend operator fun invoke() = authService.deleteUser()
}