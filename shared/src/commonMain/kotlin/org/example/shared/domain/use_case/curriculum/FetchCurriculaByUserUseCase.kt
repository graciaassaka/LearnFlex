package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.CurriculumRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for retrieving all curricula.
 *
 * @property repository The repository to retrieve curricula from.
 */
class FetchCurriculaByUserUseCase(private val repository: CurriculumRepository) {
    /**
     * Retrieves all curricula for a given user.
     *
     * @param userId The ID of the user whose curricula are to be retrieved.
     * @return A Result containing a list of curricula or an error if the retrieval fails.
     */
    suspend operator fun invoke(userId: String) = try {
        val path = PathBuilder().collection(Collection.PROFILES)
            .document(userId)
            .collection(Collection.CURRICULA)
            .build()
        withTimeout(2500L) {
            repository.getAll(path).filter { it.getOrThrow().isNotEmpty() }.first()
        }
    } catch (_: TimeoutCancellationException) {
        Result.success(emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }
}