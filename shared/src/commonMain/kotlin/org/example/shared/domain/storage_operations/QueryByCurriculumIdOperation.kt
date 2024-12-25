package org.example.shared.domain.storage_operations

import org.example.shared.domain.model.interfaces.DatabaseRecord

/**
 * Interface representing a query operation that retrieves a set of models by a curriculum ID.
 *
 * @param Model The type of the model.
 */
interface QueryByCurriculumIdOperation<Model : DatabaseRecord> {

    /**
     * Retrieves a list of models associated with the specified curriculum ID.
     *
     * @param curriculumId The ID of the curriculum used to filter the models.
     * @return A [Result] containing a list of models associated with the given curriculum ID,
     *         or an error if the operation fails.
     */
    suspend fun getByCurriculumId(curriculumId: String): Result<List<Model>>
}