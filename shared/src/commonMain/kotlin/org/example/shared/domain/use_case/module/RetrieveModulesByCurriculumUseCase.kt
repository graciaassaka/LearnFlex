package org.example.shared.domain.use_case.module

import org.example.shared.domain.repository.ModuleRepository

/**
 * Use case for retrieving modules associated with a specific curriculum ID.
 *
 * @property repository The repository to access and retrieve module data.
 */
class RetrieveModulesByCurriculumUseCase(private val repository: ModuleRepository) {
    /**
     * Retrieves a list of models associated with the specified curriculum ID.
     *
     * @param curriculumId The ID of the curriculum used to filter the models.
     */
    suspend operator fun invoke(curriculumId: String) = repository.getByCurriculumId(curriculumId)
}