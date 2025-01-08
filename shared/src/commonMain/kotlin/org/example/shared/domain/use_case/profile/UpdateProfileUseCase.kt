package org.example.shared.domain.use_case.profile

import kotlinx.coroutines.delay
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Profile
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.example.shared.domain.use_case.util.CompoundException

/**
 * Use case for updating a user's profile.
 *
 * @property repository The repository for user profiles.
 * @property authClient The client for authentication operations.
 */
class UpdateProfileUseCase(
    private val repository: ProfileRepository,
    private val authClient: AuthClient
) {
    /**
     * Updates the user's profile.
     *
     * @param profile The updated profile data.
     * @return A Result indicating the success or failure of the update operation.
     */
    suspend operator fun invoke(profile: Profile) = runCatching {
        var usernameUpdated = false
        var photoUrlUpdated = false
        val path = PathBuilder().collection(Collection.PROFILES).document(profile.id).build()

        repeat(RETRY_TIMES) { time ->
            try {
                authClient.updateUsername(profile.username).getOrThrow().also { usernameUpdated = true }
                authClient.updatePhotoUrl(profile.photoUrl).getOrThrow().also { photoUrlUpdated = true }
                repository.update(profile, path, System.currentTimeMillis()).getOrThrow()
                return@runCatching
            } catch (e: Exception) {
                rollbackProfileUpdate(usernameUpdated, photoUrlUpdated, e)
                if (time < RETRY_TIMES - 1) delay(RETRY_DELAY * (time + 1)) else throw e
            }
        }
    }

    /**
     * Rollbacks the profile update in case of an error.
     *
     * @param usernameUpdated Indicates if the username was updated.
     * @param photoUrlUpdated Indicates if the photo URL was updated.
     * @param e The exception that occurred during the update.
     */
    private suspend fun rollbackProfileUpdate(
        usernameUpdated: Boolean, photoUrlUpdated: Boolean, e: Exception
    ) = repeat(RETRY_TIMES) { time ->
        try {
            if (photoUrlUpdated) authClient.updatePhotoUrl("").getOrThrow()
            if (usernameUpdated) authClient.updateUsername("").getOrThrow()
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