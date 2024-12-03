package org.example.shared.domain.dao

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.model.definition.DatabaseRecord

/**
 * ExtendedRemoteDao is an interface that extends RemoteDao and provides additional
 * methods for batch operations on remote data sources.
 *
 * @param Model The type of the database record that extends DatabaseRecord.
 */
interface ExtendedRemoteDao<Model : DatabaseRecord> : RemoteDao<Model> {

    /**
     * Inserts a list of items at the specified path.
     *
     * @param path The path where the items should be inserted.
     * @param items The list of items to be inserted.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun insertAll(path: String, items: List<Model>): Result<Unit>

    /**
     * Updates a list of items at the specified path.
     *
     * @param path The path where the items should be updated.
     * @param items The list of items to be updated.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun updateAll(path: String, items: List<Model>): Result<Unit>

    /**
     * Deletes a list of items at the specified path.
     *
     * @param path The path where the items should be deleted.
     * @param items The list of items to be deleted.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun deleteAll(path: String, items: List<Model>): Result<Unit>

    /**
     * Retrieves all items from the specified path.
     *
     * @param path The path from which the items should be retrieved.
     * @return A Flow emitting a Result containing a list of items.
     */
    fun getAll(path: String): Flow<Result<List<Model>>>
}