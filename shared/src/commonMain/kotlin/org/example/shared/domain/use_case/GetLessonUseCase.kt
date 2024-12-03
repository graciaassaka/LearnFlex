package org.example.shared.domain.use_case

import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for retrieving a lesson.
 *
 * @property repository The repository to retrieve lesson data from.
 */
class GetLessonUseCase(private val repository: LessonRepository) {

    /**
     * Invokes the use case to get a lesson by its path and ID.
     *
     * @param path The path where the lesson is located.
     * @param lessonId The unique identifier of the lesson.
     * @return The lesson data retrieved from the repository.
     */
    operator fun invoke(path: String, lessonId: String) = repository.get(path, lessonId)
}