package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for uploading a list of lessons.
 *
 * @property repository The repository used for lesson data operations.
 */
class UploadLessonsUseCase(private val repository: LessonRepository) {
    /**
     * Uploads a list of lessons to the repository.
     *
     * @param lessons The list of lessons to be uploaded.
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @param moduleId The ID of the module.
     * @return A Result indicating the success or failure of the upload operation.
     */
    suspend operator fun invoke(
        lessons: List<Lesson>,
        userId: String,
        curriculumId: String,
        moduleId: String
    ) = try {
        repository.insertAll(
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