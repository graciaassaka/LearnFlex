package org.example.shared.domain.use_case

import org.example.shared.domain.repository.CurriculumRepository

/**
 * Use case for retrieving curriculum data.
 *
 * @property repository The repository to retrieve curriculum data from.
 */
class GetCurriculumUseCase(private val repository: CurriculumRepository) {
    /**
     * Invokes the use case to get curriculum data.
     *
     * @param path The path to the curriculum data.
     * @param id The ID of the curriculum data.
     * @return The curriculum data.
     */
    operator fun invoke(path: String, id: String) = repository.get(path, id)
}