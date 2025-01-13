package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case responsible for uploading a lesson to a specified module within a curriculum for a user.
 *
 * @property repository The repository used for interacting with lesson data storage.
 */
class UploadLessonUseCase(
    private val repository: LessonRepository
) {
    /**
     * Inserts the given lesson into the repository using a dynamically constructed path based
     * on the provided user, curriculum, and module identifiers.
     *
     * @param lesson The lesson to be uploaded to the repository.
     * @param userId The identifier of the user associated with the lesson.
     * @param curriculumId The identifier of the curriculum to which the lesson belongs.
     * @param moduleId The identifier of the module containing the lesson.
     */
    suspend operator fun invoke(
        lesson: Lesson,
        userId: String,
        curriculumId: String,
        moduleId: String
    ) = try {
        repository.insert(
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