package org.example.shared.domain.storage_operations

import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.interfaces.StatusQueryable

/**
 * Interface for querying a database for records with a specific status.
 */
interface QueryByStatusOperation<Model : StatusQueryable> {
    /**
     * Retrieves a list of models that match a specific status from the database.
     *
     * @param status The [Status] to filter the models by.
     * @return A [Result] containing a list of models matching the specified status.
     */
    suspend fun getByStatus(status: Status): Result<List<Model>>
}