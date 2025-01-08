package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for deleting all lessons.
 *
 * @property repository The repository to handle lesson data operations.
 */
class DeleteLessonsByModuleUseCase(private val repository: LessonRepository) {
    /**
     * Deletes all lessons for a given user, curriculum, and module.
     *
     * @param lessons The list of lessons to be deleted.
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @param moduleId The ID of the module.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend operator fun invoke(
        lessons: List<Lesson>,
        userId: String,
        curriculumId: String,
        moduleId: String
    ) = try {
        repository.deleteAll(
            items = lessons,
            path = PathBuilder().collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.CURRICULA)
                .document(curriculumId)
                .collection(Collection.MODULES)
                .document(moduleId)
                .collection(Collection.LESSONS)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}