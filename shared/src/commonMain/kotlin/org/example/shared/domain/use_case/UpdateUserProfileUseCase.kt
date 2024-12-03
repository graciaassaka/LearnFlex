package org.example.shared.domain.use_case

import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepository

/**
 * Use case for updating a user's profile.
 *
 * @property repository The repository for user profiles.
 * @property authClient The client for authentication operations.
 */
class UpdateUserProfileUseCase(
    private val repository: UserProfileRepository,
    private val authClient: AuthClient
) {

    /**
     * Updates the user's profile information.
     *
     * @param path The path to store the user profile.
     * @param userProfile The user profile to update.
     * @return A result of the update operation.
     */
    suspend operator fun invoke(path: String, userProfile: UserProfile) = runCatching {
        repository.update(path, userProfile).getOrThrow()
        authClient.updateUsername(userProfile.username).getOrThrow()
        authClient.updatePhotoUrl(userProfile.photoUrl).getOrThrow()
    }
}