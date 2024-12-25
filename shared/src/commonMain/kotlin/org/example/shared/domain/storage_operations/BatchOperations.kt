package org.example.shared.domain.storage_operations

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.model.interfaces.DatabaseRecord

/**
 * Interface for batch operations on a collection of database records.
 *
 * @param Model The type of the database record.
 */
interface BatchOperations<Model : DatabaseRecord> {

    /**
     * Inserts a list of items into the database at the specified path.
     *
     * @param path The path where the items should be inserted.
     * @param items The list of items to insert.
     * @param timestamp The timestamp to associate with the operation.
     *
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun insertAll(path: String, items: List<Model>, timestamp: Long): Result<Unit>

    /**
     * Updates a list of items in the database at the specified path.
     *
     * @param path The path where the items should be updated.
     * @param items The list of items to update.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun updateAll(path: String, items: List<Model>, timestamp: Long): Result<Unit>

    /**
     * Deletes a list of items from the database at the specified path.
     *
     * @param path The path where the items should be deleted.
     * @param items The list of items to delete.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun deleteAll(path: String, items: List<Model>, timestamp: Long): Result<Unit>

    /**
     * Retrieves all items from the database at the specified path.
     *
     * @param path The path from where the items should be retrieved.
     * @return A [Flow] emitting a [Result] containing the list of items.
     */
    fun getAll(path: String): Flow<Result<List<Model>>>
}