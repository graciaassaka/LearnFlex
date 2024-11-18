package org.example.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.model.contract.DatabaseRecord

/**
 * A generic repository interface for managing CRUD operations on items of type T.
 */
interface Repository<Model : DatabaseRecord> {

    /**
     * Creates a new item in the repository.
     *
     * @param item The item to be created.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun create(item: Model): Result<Unit>

    /**
     * Updates an existing item in the repository.
     *
     * @param item The item to be updated.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun update(item: Model): Result<Unit>

    /**
     * Retrieves an item from the repository by its ID.
     *
     * @param id The ID of the item to be retrieved.
     * @return A [Flow] emitting a [Result] containing the item if found, or an error if not.
     */
    fun get(id: String): Flow<Result<Model>>

    /**
     * Deletes an item from the repository.
     *
     * @param item The item to be deleted.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun delete(item: Model): Result<Unit>
}