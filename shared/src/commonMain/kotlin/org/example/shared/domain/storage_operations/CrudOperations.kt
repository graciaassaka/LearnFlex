package org.example.shared.domain.storage_operations

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.model.definition.DatabaseRecord

/**
 * A generic repository interface for managing CRUD operations on items of type Model.
 *
 * @param Model The type of the model, which must implement [DatabaseRecord].
 */
interface CrudOperations<Model : DatabaseRecord> {

    /**
     * Creates a new item in the repository.
     *
     * @param path The path in the repository where the item should be created.
     * @param item The item to be created.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun insert(path: String, item: Model, timestamp: Long): Result<Unit>

    /**
     * Updates an existing item in the repository.
     *
     * @param path The path in the repository where the item should be updated.
     * @param item The item to be updated.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun update(path: String, item: Model, timestamp: Long): Result<Unit>

    /**
     * Retrieves an item from the repository by its ID.
     *
     * @param path The path in the repository where the item should be retrieved from.
     * @param id The ID of the item to be retrieved.
     * @return A [Flow] emitting a [Result] containing the item if found, or an error if not.
     */
    fun get(path: String, id: String): Flow<Result<Model>>

    /**
     * Deletes an item from the repository.
     *
     * @param path The path in the repository where the item should be deleted from.
     * @param item The item to be deleted.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun delete(path: String, item: Model, timestamp: Long): Result<Unit>
}