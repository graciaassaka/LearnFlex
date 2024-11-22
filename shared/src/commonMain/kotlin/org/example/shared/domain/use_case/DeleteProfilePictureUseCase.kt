package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthClient
import org.example.shared.domain.service.StorageClient

/**
 * Use case for deleting a user's profile picture.
 *
 * @property storageClient The service responsible for file storage operations.
 * @property authClient The service responsible for authentication and user data operations.
 */
class DeleteProfilePictureUseCase(
    private val storageClient: StorageClient,
    private val authClient: AuthClient
) {
    /**
     * Deletes the profile picture of the currently authenticated user.
     *
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend operator fun invoke() = runCatching {
        val user = authClient.getUserData().getOrThrow()

        storageClient.deleteFile("profile_pictures/${user.localId}.jpg").getOrThrow()

        authClient.updateUserData(user.copy(photoUrl = null)).getOrThrow()
    }
}