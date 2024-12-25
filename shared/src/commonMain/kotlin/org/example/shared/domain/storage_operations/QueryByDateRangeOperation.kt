package org.example.shared.domain.storage_operations

import org.example.shared.domain.model.interfaces.EndTimeQueryable

/**
 * Interface representing a query operation that retrieves models within a specified date range.
 *
 * @param Model The type of the model, which must implement [EndTimeQueryable].
 */
interface QueryByDateRangeOperation<Model : EndTimeQueryable> {
    /**
     * Queries a list of models within a specified date range.
     *
     * @param start The start timestamp of the date range, represented in milliseconds.
     * @param end The end timestamp of the date range, represented in milliseconds.
     * @return A [Result] containing a list of models that fall within the given date range.
     */
    suspend fun queryByDateRange(start: Long, end: Long): Result<List<Model>>
}