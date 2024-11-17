package org.example.shared.domain.use_case

import org.example.shared.domain.model.StyleResult
import org.example.shared.domain.data_source.LearningStyleRemoteDataSource

/**
 * Use case for setting the user's learning style.
 *
 * @property styleRepos The repository to interact with learning styles.
 */
class SetUserStyleUseCase (private val styleRepos: LearningStyleRemoteDataSource) {

    /**
     * Sets the learning style for a user.
     *
     * @param userId The ID of the user.
     * @param style The learning style to set for the user.
     */
    suspend operator fun invoke(userId: String, style: StyleResult) = styleRepos.setLearningStyle(userId, style)
}