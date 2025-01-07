package org.example.shared.domain.use_case.module

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.ModuleRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for retrieving all modules.
 *
 * @property repository The repository to retrieve module data from.
 */
class FetchModulesByCurriculumUseCase(private val repository: ModuleRepository) {
    /**
     * Retrieves all modules from the repository.
     *
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @return A Result containing a list of modules or an error if the retrieval fails.
     */
    suspend operator fun invoke(
        userId: String,
        curriculumId: String
    ) = try {
        val path = PathBuilder().collection(Collection.PROFILES)
            .document(userId)
            .collection(Collection.CURRICULA)
            .document(curriculumId)
            .collection(Collection.MODULES)
            .build()
        withTimeout(500L) {
            repository.getAll(path).filter { it.getOrThrow().isNotEmpty() }.first()
        }
    } catch (_: TimeoutCancellationException) {
        Result.success(emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }
}