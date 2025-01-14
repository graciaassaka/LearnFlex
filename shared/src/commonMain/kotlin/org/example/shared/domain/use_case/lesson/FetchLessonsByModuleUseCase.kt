package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.TimeoutCancellationException
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.LessonRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for retrieving all lessons.
 *
 * @property repository The repository to retrieve lesson data from.
 */
class FetchLessonsByModuleUseCase(private val repository: LessonRepository) {
    /**
     * Retrieves all lessons for a given user, curriculum, and module.
     *
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @param moduleId The ID of the module.
     * @return A Result containing a list of lessons or an error if the retrieval fails.
     */
    suspend operator fun invoke(
        userId: String,
        curriculumId: String,
        moduleId: String
    ) = try {
        repository.getAll(
            PathBuilder()
                .collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.CURRICULA)
                .document(curriculumId)
                .collection(Collection.MODULES)
                .document(moduleId)
                .collection(Collection.LESSONS)
                .build()
        )
    } catch (_: TimeoutCancellationException) {
        Result.success(emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }
}