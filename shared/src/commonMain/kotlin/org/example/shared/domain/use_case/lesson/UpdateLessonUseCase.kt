package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for updating a lesson.
 *
 * @property repository The repository to update the lesson.
 */
class UpdateLessonUseCase(private val repository: LessonRepository) {
    /**
     * Updates a lesson in the repository.
     *
     * @param lesson The lesson to update.
     * @param userId The user ID.
     * @param curriculumId The curriculum ID.
     * @param moduleId The module ID.
     * @return A [Result] indicating the success or failure of the update operation.
     */
    suspend operator fun invoke(
        lesson: Lesson,
        userId: String,
        curriculumId: String,
        moduleId: String
    ) = try {
        repository.update(
            item = lesson,
            path = PathBuilder().collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.CURRICULA)
                .document(curriculumId)
                .collection(Collection.MODULES)
                .document(moduleId)
                .collection(Collection.LESSONS)
                .document(lesson.id)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}