package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for updating a lesson.
 *
 * @property repository The repository to update the lesson.
 */
class UpdateLessonUseCase(private val repository: LessonRepository) {

    /**
     * Updates a lesson.
     *
     * @param path The path of the lesson to update.
     * @param lesson The lesson object containing updated information.
     */
    suspend operator fun invoke(path: String, lesson: Lesson) =
        repository.update(path, lesson, System.currentTimeMillis())
}