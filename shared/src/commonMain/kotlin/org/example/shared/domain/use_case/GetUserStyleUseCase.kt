package org.example.shared.domain.use_case

import org.example.shared.domain.data_source.LearningStyleRemoteDataSource

/**
 * Use case for getting the user's style.
 *
 * @property styleRepos The repository to get the user's style.
 */
class GetUserStyleUseCase(private val styleRepos: LearningStyleRemoteDataSource) {

    /**
     * Invokes the use case to get the user's style.
     *
     * @param userId The ID of the user to get the style.
     * @return The user's style.
     */
    suspend operator fun invoke(userId: String) = styleRepos.fetchLearningStyle(userId)
}