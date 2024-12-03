package org.example.shared.domain.use_case

import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for deleting all lessons.
 *
 * @property repository The repository to handle lesson data operations.
 */
class DeleteAllLessonsUseCase(private val repository: LessonRepository) {

    /**
     * Deletes a list of lessons from the repository at the specified path.
     *
     * @param path The path where the lessons should be deleted.
     * @param lessons The list of lessons to delete.
     */
    suspend operator fun invoke(path: String, lessons: List<Lesson>) = repository.deleteAll(path, lessons)
}