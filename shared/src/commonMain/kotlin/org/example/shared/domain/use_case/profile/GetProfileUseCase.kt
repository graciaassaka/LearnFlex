package org.example.shared.domain.use_case.profile

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
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
     * Retrieves a user's profile by first getting their user data and then
     * fetching their profile from the repository.
     *
     * @param path The repository path where profiles are stored
     * @return A Flow emitting Results containing either the Profile or an error
     */
    operator fun invoke(path: String) = flow {
        val userData = getUserDataUseCase().getOrThrow()
        val userId = requireNotNull(userData.localId) {
            USER_ID_REQUIRED_MESSAGE
        }

        repository.get(path, userId).collect { result ->
            emit(result)
        }
    }.catch { error ->
        emit(Result.failure(error))
    }

    companion object {
        const val USER_ID_REQUIRED_MESSAGE = "User ID is required but was null"
    }
}