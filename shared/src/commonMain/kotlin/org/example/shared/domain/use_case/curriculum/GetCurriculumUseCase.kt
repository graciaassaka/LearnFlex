package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.flow.first
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for retrieving curriculum data.
 *
 * @property repository The repository to retrieve curriculum data from.
 */
class GetCurriculumUseCase(private val repository: CurriculumRepository) {

    /**
     * Retrieves a curriculum by its ID from the specified path.
     * Ensures that the path ends with the value of [Collection.CURRICULA].
     *
     * @param path The path in the repository where the curriculum should be retrieved from.
     * Must end with the value of [Collection.CURRICULA].
     * @param id The ID of the curriculum to retrieve.
     * @return A [Result] containing the retrieved curriculum, or an error if the operation fails.
     * @throws IllegalArgumentException If the path does not end with [Collection.CURRICULA].
     */
    suspend operator fun invoke(path: String, id: String) = runCatching {
        require(path.split("/").last() == Collection.CURRICULA.value) {
            "The path must end with ${Collection.CURRICULA.value}"
        }
        repository.get(path, id).first().getOrThrow()
    }
}
