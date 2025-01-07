package org.example.shared.domain.use_case.module

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for uploading modules.
 *
 * @property repository The repository to interact with module data.
 */
class UploadModulesUseCase(private val repository: ModuleRepository) {
    /**
     * Uploads a list of modules to the repository.
     *
     * @param modules The list of modules to be uploaded.
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @return A Result indicating the success or failure of the upload operation.
     */
    suspend operator fun invoke(
        modules: List<Module>,
        userId: String,
        curriculumId: String
    ) = try {
        repository.insertAll(
            items = modules,
            path = PathBuilder()
                .collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.CURRICULA)
                .document(curriculumId)
                .collection(Collection.MODULES)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}