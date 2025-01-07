package org.example.shared.domain.use_case.profile

import kotlinx.coroutines.delay
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Profile
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.example.shared.domain.use_case.util.CompoundException

/**
 * Use case for creating a user profile.
 *
 * @property repository The repository used to create the user profile.
 * @property authClient The service used to handle authentication and user data.
 */
class CreateProfileUseCase(
    private val repository: ProfileRepository,
    private val authClient: AuthClient
) {

    /**
     * Creates a user profile.
     *
     * @param profile The profile to be created.
     * @return A Result indicating the success or failure of the profile creation operation.
     */
    suspend operator fun invoke(profile: Profile) = runCatching {
        val path = PathBuilder().collection(Collection.PROFILES).document(profile.id).build()
        var isUsernameUpdated = false
        var isPhotoUrlUpdated = false

        repeat(RETRY_TIMES) { time ->
            try {
                authClient.updateUsername(profile.username).getOrThrow().also { isUsernameUpdated = true }
                authClient.updatePhotoUrl(profile.photoUrl).getOrThrow().also { isPhotoUrlUpdated = true }
                repository.insert(profile, path).getOrThrow()
                return@runCatching
            } catch (e: Exception) {
                rollbackProfileUpdate(isUsernameUpdated, isPhotoUrlUpdated, e)
                if (time < RETRY_TIMES - 1) delay(RETRY_DELAY * (time + 1)) else throw e
            }
        }
    }

    /**
     * Rollback the profile update in case of failure.
     *
     * @param usernameUpdated Indicates if the username was updated.
     * @param photoUrlUpdated Indicates if the photo URL was updated.
     * @param e The exception that caused the rollback.
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
            else throw CompoundException(ROLLBACK_ERROR_MESSAGE, e, rollbackError)
        }
    }

    companion object {
        const val RETRY_TIMES = 3
        const val RETRY_DELAY = 1000L
        const val ROLLBACK_ERROR_MESSAGE = "Failed to create profile and rollback failed"
    }
}