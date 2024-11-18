package org.example.shared.domain.use_case

import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.repository.Repository

/**
 * Use case for getting the user's style.
 *
 * @property repository The repository to get the user's style.
 */
class GetUserStyleUseCase(private val repository: Repository<LearningStyle>) {

    /**
     * Invokes the use case to get the user's style.
     *
     * @param userId The ID of the user to get the style.
     * @return The user's style.
     */
    operator fun invoke(userId: String) = repository.get(userId)
}