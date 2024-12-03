package org.example.shared.domain.use_case

import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for updating a module.
 *
 * @property repository The repository used to update the module.
 */
class UpdateModuleUseCase(private val repository: ModuleRepository) {

    /**
     * Invokes the use case to update a module in the specified path.
     *
     * @param path The path where the module is located.
     * @param module The module object with updated information.
     */
    suspend operator fun invoke(path: String, module: Module) = repository.update(path, module)
}