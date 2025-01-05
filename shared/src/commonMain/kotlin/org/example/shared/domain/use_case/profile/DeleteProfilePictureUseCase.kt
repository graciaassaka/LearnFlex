package org.example.shared.domain.use_case.profile

import kotlinx.coroutines.delay
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.client.StorageClient
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.util.CompoundException

/**
 * Use case for deleting a user's profile picture.
 *
 * @property storageClient The service responsible for file storage operations.
 * @property authClient The service responsible for authentication and user data operations.
 */
class DeleteProfilePictureUseCase(
    private val storageClient: StorageClient,
    private val authClient: AuthClient,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) {
    /**
     * Deletes the user's profile picture.
     *
     * @param path The path to store the user profile.
     */
    suspend operator fun invoke(path: String) = runCatching {
        val user = authClient.getUserData().getOrThrow()
        val profile = getProfileUseCase(path).getOrThrow()

        var isProfileUpdated = false
        var isAuthUpdated = false
        repeat(RETRY_TIMES) { time ->
            try {
                authClient.updatePhotoUrl("").getOrThrow().also { isAuthUpdated = true }
                updateProfileUseCase(path, profile.copy(photoUrl = "")).getOrThrow().also { isProfileUpdated = true }
                storageClient.deleteFile("profile_pictures/${user.localId}.jpg").getOrThrow()
                return@runCatching
            } catch (e: Exception) {
                rollbackProfilePictureUpdate(path, profile, isProfileUpdated, isAuthUpdated, e)
                if (time < RETRY_TIMES - 1) delay(RETRY_DELAY * (time + 1))
                else throw e
            }
        }
    }

    private suspend fun rollbackProfilePictureUpdate(
        path: String, profile: Profile, isProfileUpdated: Boolean, isAuthUpdated: Boolean, e: Exception
    ) = repeat(RETRY_TIMES) { time ->
        try {
            if (isProfileUpdated) updateProfileUseCase(path, profile).getOrThrow()
            if (isAuthUpdated) authClient.updatePhotoUrl(profile.photoUrl).getOrThrow()
            return
        } catch (rollbackError: Exception) {
            if (time < RETRY_TIMES - 1) delay(RETRY_DELAY * (time + 1))
            else throw CompoundException(ROLLBACK_ERROR_MESSAGE, e, rollbackError)
        }
    }

    companion object {
        const val RETRY_TIMES = 3
        const val RETRY_DELAY = 1000L
        const val ROLLBACK_ERROR_MESSAGE = "Failed to update profile and rollback failed"
    }
}