package org.example.shared.domain.use_case

import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for retrieving section IDs based on a minimum quiz score.
 *
 * @property repository The repository to interact with section data.
 */
class GetSectionIdsByMinQuizScoreUseCase(private val repository: SectionRepository) {

    /**
     * Invokes the use case to retrieve a set of section IDs that have a quiz score greater than or equal to the specified minimum score.
     *
     * @param parentId The ID of the parent entity for which section IDs are being retrieved.
     * @param minQuizScore The minimum quiz score to filter sections by.
     * @return A [Flow] emitting a [Result] containing the set of section IDs that meet the score criteria.
     */
    operator fun invoke(parentId: String, minQuizScore: Int) = repository.getIdsByMinScore(parentId, minQuizScore)
}