package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for uploading a list of lessons.
 *
 * @property repository The repository used for lesson data operations.
 */
class UploadLessonsUseCase(private val repository: LessonRepository) {

    /**
     * Inserts a list of lessons into the repository at the specified path.
     *
     * @param path The path where the lessons should be inserted.
     * @param lessons The list of `Lesson` objects to insert.
     */
    suspend operator fun invoke(path: String, lessons: List<Lesson>) =
        repository.insertAll(path, lessons, System.currentTimeMillis())
}