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
     * @param item The item to be created.
     * @return A [Result] indicating success or failure.
     */
    suspend fun create(item: Model): Result<Unit>

    /**
     * Fetches an item from the remote data source by its ID.
     *
     * @param id The ID of the item to be fetched.
     * @return A [Result] containing the fetched item or an error.
     */
    suspend fun fetch(id: String): Result<Model>

    /**
     * Deletes an item from the remote data source by its ID.
     *
     * @param id The ID of the item to be deleted.
     * @return A [Result] indicating success or failure.
     */
    suspend fun delete(id: String): Result<Unit>
}