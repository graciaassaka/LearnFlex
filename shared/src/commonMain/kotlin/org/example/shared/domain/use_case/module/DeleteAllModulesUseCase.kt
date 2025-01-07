package org.example.shared.domain.use_case.module

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for deleting all modules.
 *
 * @property repository The repository to perform deletion of modules.
 */
class DeleteAllModulesUseCase(private val repository: ModuleRepository) {
    /**
     * Deletes all modules for a given user and curriculum.
     *
     * @param modules The list of modules to be deleted.
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @return A Result indicating the success or failure of the deletion operation.
     */
    suspend operator fun invoke(
        modules: List<Module>,
        userId: String,
        curriculumId: String
    ) = try {
        repository.deleteAll(
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