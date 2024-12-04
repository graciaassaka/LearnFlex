package org.example.shared.domain.use_case

import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for retrieving lesson IDs that meet a minimum quiz score criterion.
 *
 * @property repository The repository to fetch lesson data from.
 */
class GetLessonIdsByMinQuizScoreUseCase(private val repository: LessonRepository) {

    /**
     * Invokes the use case to retrieve a set of lesson IDs based on a minimum quiz score.
     *
     * @param parentId The ID of the parent entity.
     * @param minQuizScore The minimum quiz score to filter the lesson IDs by.
     */
    operator fun invoke(parentId: String, minQuizScore: Int) = repository.getIdsByMinScore(parentId, minQuizScore)
}