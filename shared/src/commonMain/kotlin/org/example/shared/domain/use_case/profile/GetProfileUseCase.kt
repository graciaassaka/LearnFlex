package org.example.shared.domain.use_case.profile

import kotlinx.coroutines.flow.first
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.use_case.auth.GetUserDataUseCase

/**
 * Use case for getting a user's profile.
 *
 * @property getUserDataUseCase The use case for getting user data.
 * @property repository The repository for user profiles.
 */
class GetProfileUseCase(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val repository: ProfileRepository
) {

    /**
     * Invokes the use case to get a user's profile.
     *
     * @param path The path to the user's profile.
     * @return The result of the profile fetch operation.
     */
    suspend operator fun invoke(path: String) = runCatching {
        val userData = getUserDataUseCase().getOrThrow()
        val userId = requireNotNull(userData.localId) {
            USER_ID_REQUIRED_MESSAGE
        }

        repository.get(path, userId).first().getOrThrow()
    }

    companion object {
        const val USER_ID_REQUIRED_MESSAGE = "User ID is required but was null"
    }
}