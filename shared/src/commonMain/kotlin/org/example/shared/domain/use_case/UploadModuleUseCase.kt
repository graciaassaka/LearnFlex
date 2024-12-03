package org.example.shared.domain.use_case

import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for uploading a module.
 *
 * @property repository The repository to interact with module data.
 */
class UploadModuleUseCase(private val repository: ModuleRepository) {

    /**
     * Invokes the use case to upload a module.
     *
     * @param path The path where the module should be uploaded.
     * @param module The module to be uploaded.
     */
    suspend operator fun invoke(path: String, module: Module) = repository.insert(path, module)
}