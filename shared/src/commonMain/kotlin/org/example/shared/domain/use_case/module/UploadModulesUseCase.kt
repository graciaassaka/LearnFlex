package org.example.shared.domain.use_case.module

import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for uploading modules.
 *
 * @property repository The repository to interact with module data.
 */
class UploadModulesUseCase(private val repository: ModuleRepository) {

    /**
     * Invokes the use case to upload modules.
     *
     * @param path The path where the module should be uploaded.
     * @param modules The modules to be uploaded.
     */
    suspend operator fun invoke(path: String, modules: List<Module>) =
        repository.insertAll(path, modules, System.currentTimeMillis())
}