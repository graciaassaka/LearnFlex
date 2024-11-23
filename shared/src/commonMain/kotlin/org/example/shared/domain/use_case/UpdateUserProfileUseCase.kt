package org.example.shared.domain.use_case

import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository

class UpdateUserProfileUseCase(private val repository: Repository<UserProfile>) {

    /**
     * Updates the user's profile information.
     *
     */
    suspend operator fun invoke(userProfile: UserProfile) = repository.update(userProfile)
}