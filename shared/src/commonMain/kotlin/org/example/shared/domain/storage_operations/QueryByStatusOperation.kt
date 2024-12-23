package org.example.shared.domain.storage_operations

import org.example.shared.domain.constant.ContentStatus
import org.example.shared.domain.model.definition.StatusQueryable

/**
 * Interface for querying a database for records with a specific status.
 */
interface QueryByStatusOperation<Model : StatusQueryable> {
    /**
     * Retrieves a list of models that match a specific status from the database.
     *
     * @param status The [ContentStatus] to filter the models by.
     * @return A [Result] containing a list of models matching the specified status.
     */
    suspend fun getByStatus(status: ContentStatus): Result<List<Model>>
}