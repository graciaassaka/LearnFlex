package org.example.shared.domain.data_source

/**
 * Interface representing a remote data source for a specific model type.
 *
 * @param Model The type of the model.
 */
interface RemoteDataSource<Model> {
    /**
     * Creates a new item in the remote data source.
     *
     * @param path The path in the remote data source where the item should be created.
     * @param item The item to be created.
     * @return A [Result] indicating success or failure.
     */
    suspend fun create(path: String, item: Model): Result<Unit>

    /**
     * Updates an existing item in the remote data source.
     *
     * @param path The path in the remote data source where the item should be updated.
     * @param item The item to be updated.
     * @return A [Result] indicating success or failure.
     */
    suspend fun update(path: String, item: Model): Result<Unit>

    /**
     * Fetches an item from the remote data source by its ID.
     *
     * @param path The path in the remote data source where the item should be fetched from.
     * @param id The ID of the item to be fetched.
     * @return A [Result] containing the fetched item or an error.
     */
    suspend fun fetch(path: String, id: String): Result<Model>

    /**
     * Deletes an item from the remote data source by its ID.
     *
     * @param path The path in the remote data source where the item should be deleted from.
     * @param id The ID of the item to be deleted.
     * @return A [Result] indicating success or failure.
     */
    suspend fun delete(path: String, id: String): Result<Unit>
}