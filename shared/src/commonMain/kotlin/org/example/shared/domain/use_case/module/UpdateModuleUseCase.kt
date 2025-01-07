package org.example.shared.domain.use_case.module

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for updating a module.
 *
 * @property repository The repository used to update the module.
 */
class UpdateModuleUseCase(private val repository: ModuleRepository) {
    /**
     * Updates a module in the repository.
     *
     * @param module The module to be updated.
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @return A Result indicating the success or failure of the update operation.
     */
    suspend operator fun invoke(
        module: Module,
        userId: String,
        curriculumId: String
    ) = try {
        repository.update(
            item = module,
            path = PathBuilder()
                .collection(Collection.PROFILES)
                .document(userId)
                .collection(Collection.CURRICULA)
                .document(curriculumId)
                .collection(Collection.MODULES)
                .document(module.id)
                .build()
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}