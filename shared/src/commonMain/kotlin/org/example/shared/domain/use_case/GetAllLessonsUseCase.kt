package org.example.shared.domain.use_case

import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for retrieving all lessons.
 *
 * @property repository The repository to retrieve lesson data from.
 */
class GetAllLessonsUseCase(private val repository: LessonRepository) {

    /**
     * Invokes the use case to retrieve all lessons from the specified path.
     *
     * @param path The path from where the lessons should be retrieved.
     * @return A [Flow] emitting a [Result] containing the list of lessons.
     */
    operator fun invoke(path: String) = repository.getAll(path)
}