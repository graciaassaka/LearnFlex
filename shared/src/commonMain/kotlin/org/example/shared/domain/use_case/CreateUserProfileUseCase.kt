package org.example.shared.domain.use_case

import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.data_source.PathBuilder
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository

/**
 * Use case for creating a user profile.
 *
 * @property repository The repository used to create the user profile.
 * @property authClient The service used to handle authentication and user data.
 */
class CreateUserProfileUseCase(
    private val repository: Repository<UserProfile>,
    private val authClient: AuthClient,
    private val pathBuilder: PathBuilder
) {
    /**
     * Invokes the use case to create a new user profile.
     *
     * @param userProfile The user profile to be created.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend operator fun invoke(userProfile: UserProfile) = runCatching {
        repository.create(pathBuilder.buildUserPath(), userProfile).getOrThrow()
        authClient.updateUsername(userProfile.username).getOrThrow()
        authClient.updatePhotoUrl(userProfile.photoUrl).getOrThrow()
    }
}