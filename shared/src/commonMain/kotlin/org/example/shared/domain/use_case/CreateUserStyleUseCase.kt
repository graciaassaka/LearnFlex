package org.example.shared.domain.use_case

import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.repository.Repository

/**
 * Use case for setting the user's learning style.
 *
 * @property repository The repository to interact with learning styles.
 */
class CreateUserStyleUseCase(private val repository: Repository<LearningStyle>) {

    /**
     * Sets the learning style for a user.
     *
     */
    suspend operator fun invoke(learningStyle: LearningStyle) = repository.create(learningStyle)
}