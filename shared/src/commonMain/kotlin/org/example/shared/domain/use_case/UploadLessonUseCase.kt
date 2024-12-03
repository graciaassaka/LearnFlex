package org.example.shared.domain.use_case

import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository

/**
 * Use case for uploading a lesson.
 *
 * @property repository The repository to interact with lesson data.
 */
class UploadLessonUseCase(private val repository: LessonRepository) {

    /**
     * Invokes the use case to upload a lesson.
     *
     * @param path The path where the lesson will be uploaded.
     * @param lesson The lesson to be uploaded.
     */
    suspend operator fun invoke(path: String, lesson: Lesson) = repository.insert(path, lesson)
}