package org.example.shared.domain.use_case.section

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for retrieving all sections.
 *
 * @property repository The repository to retrieve section data from.
 */
class GetAllSectionsUseCase(private val repository: SectionRepository) {

    /**
     * Retrieves the first non-empty sections list from the specified path.
     *
     * @param path The path where the sections should be retrieved.
     * The path must end with the value of [Collection.SECTIONS].
     * @return A [Result] containing the first non-empty list of sections or an empty list if none are found.
     * @throws IllegalArgumentException If the path does not end with [Collection.SECTIONS].
     */
    suspend operator fun invoke(path: String) = runCatching {
        require(path.split("/").last() == Collection.SECTIONS.value) {
            "The path must end with ${Collection.SECTIONS.value}"
        }
        withTimeoutOrNull(500L) { repository.getAll(path).filter { it.getOrThrow().isNotEmpty() == true }.first() }
            ?.getOrNull()
            ?: emptyList()
    }
}