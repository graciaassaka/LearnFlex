package org.example.shared.domain.storage_operations

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.constant.definition.Status
import org.example.shared.domain.model.definition.StatusQueryable

/**
 * Interface for querying a database for records with a specific status.
 */
interface QueryByStatusOperation<Model : StatusQueryable> {
    /**
     * Returns a flow of results containing all records with the given status.
     *
     * @param status The status to query by.
     * @return A flow of results containing all records with the given status.
     */
    fun getByStatus(status: Status): Flow<Result<List<Model>>>
}