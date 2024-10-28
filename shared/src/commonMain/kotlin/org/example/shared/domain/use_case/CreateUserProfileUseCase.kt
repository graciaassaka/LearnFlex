package org.example.shared.domain.use_case

import org.example.shared.data.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepos

/**
 * Use case for creating a user profile.
 *
 * @property userProfileRepos The repository interface for user profile operations.
 */
class CreateUserProfileUseCase(private val userProfileRepos: UserProfileRepos)
{
    /**
     * Invokes the use case to create a new user profile.
     *
     * @param userProfile The user profile to be created.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend operator fun invoke(userProfile: UserProfile) = userProfileRepos.createUserProfile(userProfile)
}