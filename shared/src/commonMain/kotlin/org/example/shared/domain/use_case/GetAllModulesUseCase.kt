package org.example.shared.domain.use_case

import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for retrieving all modules.
 *
 * @property repository The repository to retrieve module data from.
 */
class GetAllModulesUseCase(private val repository: ModuleRepository) {

    /**
     * Invokes the use case to retrieve all modules from the specified path.
     *
     * @param path The path from where the modules should be retrieved.
     * @return A [Flow] emitting a [Result] containing the list of modules.
     */
    operator fun invoke(path: String) = repository.getAll(path)
}