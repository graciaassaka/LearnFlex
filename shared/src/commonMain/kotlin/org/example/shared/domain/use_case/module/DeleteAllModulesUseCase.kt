package org.example.shared.domain.use_case.module

import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for deleting all modules.
 *
 * @property repository The repository to perform deletion of modules.
 */
class DeleteAllModulesUseCase(private val repository: ModuleRepository) {

    /**
     * Invokes the use case to delete all specified modules at a given path.
     *
     * @param path The path where the modules should be deleted.
     * @param modules The list of modules to be deleted.
     */
    suspend operator fun invoke(path: String, modules: List<Module>) =
        repository.deleteAll(path, modules, System.currentTimeMillis())
}