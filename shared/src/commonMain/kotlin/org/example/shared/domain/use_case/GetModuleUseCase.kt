package org.example.shared.domain.use_case

import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for retrieving a module.
 *
 * @property repository The repository to retrieve the module from.
 */
class GetModuleUseCase(private val repository: ModuleRepository) {

    /**
     * Retrieves a module by its path and id.
     *
     * @param path The path of the module.
     * @param id The id of the module.
     * @return The module retrieved from the repository.
     */
    operator fun invoke(path: String, id: String) = repository.get(path, id)
}