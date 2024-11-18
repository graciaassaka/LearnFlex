package org.example.shared.domain.use_case

import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository

/**
 * Use case for creating a user profile.
 *
 * @param repository The repository for user profile operations.
 */
class CreateUserProfileUseCase(private val repository: Repository<UserProfile>) {
    /**
     * Invokes the use case to create a new user profile.
     *
     * @param userProfile The user profile to be created.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend operator fun invoke(userProfile: UserProfile) = repository.create(userProfile)
}