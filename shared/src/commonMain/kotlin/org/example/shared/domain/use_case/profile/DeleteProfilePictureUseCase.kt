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
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) {
    /**
     * Deletes the user's profile picture.
     *
     * @return A Result indicating the success or failure of the deletion operation.
     */
    suspend operator fun invoke() = runCatching {
        val user = authClient.getUserData().getOrThrow()
        val profile = fetchProfileUseCase().getOrThrow()

        var isProfileUpdated = false
        var isAuthUpdated = false
        repeat(RETRY_TIMES) { time ->
            try {
                authClient.updatePhotoUrl("").getOrThrow().also { isAuthUpdated = true }
                updateProfileUseCase(profile.copy(photoUrl = "")).getOrThrow().also { isProfileUpdated = true }
                storageClient.deleteFile("profile_pictures/${user.localId}.jpg").getOrThrow()
                return@runCatching
            } catch (e: Exception) {
                rollbackProfilePictureUpdate(profile, isProfileUpdated, isAuthUpdated, e)
                if (time < RETRY_TIMES - 1) delay(RETRY_DELAY * (time + 1))
                else throw e
            }
        }
    }

    /**
     * Rollback the profile picture update in case of failure.
     *
     * @param profile The profile to be updated.
     * @param isProfileUpdated Indicates if the profile was updated.
     * @param isAuthUpdated Indicates if the authentication data was updated.
     * @param e The exception that caused the rollback.
     */
    private suspend fun rollbackProfilePictureUpdate(
        profile: Profile, isProfileUpdated: Boolean, isAuthUpdated: Boolean, e: Exception
    ) = repeat(RETRY_TIMES) { time ->
        try {
            if (isProfileUpdated) updateProfileUseCase(profile)
            if (isAuthUpdated) authClient.updatePhotoUrl(profile.photoUrl).getOrThrow()
            return
        } catch (rollbackError: Exception) {
            if (time < RETRY_TIMES - 1) delay(RETRY_DELAY * (time + 1))
            else throw CompoundException(e, rollbackError)
        }
    }

    companion object {
        const val RETRY_TIMES = 3
        const val RETRY_DELAY = 1000L
    }
}