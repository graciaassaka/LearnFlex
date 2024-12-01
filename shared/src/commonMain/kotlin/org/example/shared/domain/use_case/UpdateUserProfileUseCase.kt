package org.example.shared.domain.use_case

import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.data_source.PathBuilder
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository

class UpdateUserProfileUseCase(
    private val repository: Repository<UserProfile>,
    private val authClient: AuthClient,
    private val pathBuilder: PathBuilder
) {

    /**
     * Updates the user's profile information.
     *
     */
    suspend operator fun invoke(userProfile: UserProfile) = runCatching {
        repository.update(pathBuilder.buildUserPath(), userProfile).getOrThrow()
        authClient.updateUsername(userProfile.username).getOrThrow()
        authClient.updatePhotoUrl(userProfile.photoUrl).getOrThrow()
    }
}