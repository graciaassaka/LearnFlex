package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for retrieving all curricula.
 *
 * @property repository The repository to retrieve curricula from.
 */
class GetAllCurriculaUseCase(private val repository: CurriculumRepository) {

    /**
     * Retrieves the first non-empty curricula list from the specified path.
     *
     * @param path The path where the curricula should be retrieved.
     * The path must end with the value of [Collection.CURRICULA].
     * @return A [Result] containing the first non-empty list of curricula or an empty list if none are found.
     * @throws IllegalArgumentException If the path does not end with [Collection.CURRICULA].
     */
    suspend operator fun invoke(path: String) = runCatching {
        require(path.split("/").last() == Collection.CURRICULA.value) {
            "The path must end with ${Collection.CURRICULA.value}"
        }
        withTimeoutOrNull(500L) { repository.getAll(path).filter { it.getOrThrow().isNotEmpty() == true }.first() }
            ?.getOrNull()
            ?: emptyList()
    }
}