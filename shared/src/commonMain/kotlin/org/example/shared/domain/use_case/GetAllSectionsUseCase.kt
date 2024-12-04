package org.example.shared.domain.use_case

import org.example.shared.domain.repository.SectionRepository

/**
 * Use case for retrieving all sections.
 *
 * @property repository The repository to retrieve section data from.
 */
class GetAllSectionsUseCase(private val repository: SectionRepository) {

    /**
     * Invokes the use case to retrieve all sections from the specified path.
     *
     * @param path The path from where the sections should be retrieved.
     * @return A [Flow] emitting a [Result] containing the list of sections.
     */
    operator fun invoke(path: String) = repository.getAll(path)
}