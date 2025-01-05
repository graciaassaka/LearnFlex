package org.example.shared.domain.use_case.module

import kotlinx.coroutines.flow.first
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for retrieving a module.
 *
 * @property repository The repository to retrieve the module from.
 */
class GetModuleUseCase(private val repository: ModuleRepository) {

    /**
     * Retrieves a module by its ID.
     *
     * @param path The path in the repository where the module should be retrieved from.
     * @param id The ID of the module to be retrieved.
     * @return The module data.
     */
    suspend operator fun invoke(path: String, id: String) = runCatching {
        require(path.split("/").last() == Collection.MODULES.value) {
            "The path must end with ${Collection.MODULES.value}"
        }
        repository.get(path, id).first().getOrThrow()
    }
}