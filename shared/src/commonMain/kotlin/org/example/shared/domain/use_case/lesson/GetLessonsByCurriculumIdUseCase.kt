package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for retrieving lessons by curriculum ID.
 *
 * This use case interacts with the [LessonRepository] to fetch a list of lessons associated
 * with a specific curriculum identifier. It serves as an abstraction layer to simplify the
 * retrieval of lessons by curriculum ID for higher-level operations.
 *
 * @property repository The repository used to fetch lesson data.
 */
class GetLessonsByCurriculumIdUseCase(private val repository: LessonRepository) {
    /**
     * Retrieves a list of lessons associated with the specified curriculum ID.
     *
     * @param curriculumId The ID of the curriculum for which lessons should be retrieved.
     * @return A [Result] containing a list of lessons corresponding to the given curriculum ID,
     *         or an error if the operation fails.
     */
    suspend operator fun invoke(curriculumId: String) = repository.getByCurriculumId(curriculumId)
}