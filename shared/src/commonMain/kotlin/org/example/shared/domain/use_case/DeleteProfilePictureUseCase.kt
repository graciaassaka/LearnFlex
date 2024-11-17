package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthService
import org.example.shared.domain.service.StorageService

/**
 * Use case for deleting a user's profile picture.
 *
 * @property storageService The service responsible for file storage operations.
 * @property authService The service responsible for authentication and user data operations.
 */
class DeleteProfilePictureUseCase(
    private val storageService: StorageService,
    private val authService: AuthService
) {
    /**
     * Deletes the profile picture of the currently authenticated user.
     *
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend operator fun invoke() = runCatching {
        val user = authService.getUserData().getOrThrow()

        storageService.deleteFile("profile_pictures/${user.localId}.jpg").getOrThrow()

        authService.updateUserData(user.copy(photoUrl = null)).getOrThrow()
    }
}