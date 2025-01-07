package org.example.shared.domain.use_case.profile

import kotlinx.coroutines.flow.first
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.example.shared.domain.use_case.auth.GetUserDataUseCase

/**
 * Use case for getting a user's profile.
 *
 * @property getUserDataUseCase The use case for getting user data.
 * @property repository The repository for user profiles.
 */
class FetchProfileUseCase(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val repository: ProfileRepository
) {
    /**
     * Fetches the user's profile.
     *
     * @return A Result containing the user's profile or an error.
     */
    suspend operator fun invoke() = try {
        repository.get(
            path = PathBuilder()
                .collection(Collection.PROFILES)
                .document(getUserDataUseCase().getOrThrow().localId)
                .build()
        ).first()
    } catch (e: Exception) {
        Result.failure(e)
    }
}