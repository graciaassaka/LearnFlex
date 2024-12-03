package org.example.shared.domain.use_case

import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for retrieving all curricula.
 *
 * @property repository The repository to retrieve curricula from.
 */
class GetAllCurriculaUseCase(private val repository: CurriculumRepository) {
    /**
     * Invokes the use case to get all curricula.
     *
     * @param path The path to retrieve curricula from.
     * @return The list of all curricula.
     */
    operator fun invoke(path: String) = repository.getAll(path)
}