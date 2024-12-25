package org.example.shared.data.repository.component

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.storage_operations.CrudOperations
import org.example.shared.domain.sync.SyncOperation

/**
 * A repository component for CRUD operations on models and entities.
 *
 * @param Model The type of the model.
 * @param Entity The type of the entity.
 * @param config The repository configuration.
 */
class CrudRepositoryComponent<Model : DatabaseRecord, Entity : RoomEntity>(
    private val config: RepositoryConfig<Model, Entity>
) : CrudOperations<Model> {
    /**
     * Creates a new item in the repository.
     *
     * @param path The path in the repository where the item should be created.
     * @param item The model to be created.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun insert(path: String, item: Model, timestamp: Long): Result<Unit> = config.runCatching {
        localDao.insert(path, modelMapper.toEntity(item), timestamp)
        syncManager.queueOperation(createSyncOperation(SyncOperation.SyncOperationType.INSERT, path, listOf(item), timestamp))
    }

    /**
     * Updates an existing item in the repository.
     *
     * @param path The path in the repository where the item should be updated.
     * @param item The model to be updated.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun update(path: String, item: Model, timestamp: Long): Result<Unit> = config.runCatching {
        localDao.update(path, modelMapper.toEntity(item), timestamp)
        syncManager.queueOperation(createSyncOperation(SyncOperation.SyncOperationType.UPDATE, path, listOf(item), timestamp))
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param path The path in the repository where the item should be retrieved from.
     * @param id The ID of the item to be retrieved.
     * @return A [Flow] emitting the [Result] containing the model or an error.
     */
    override fun get(path: String, id: String) = channelFlow {
        launch { observeLocalData(path, id) }
        launch { fetchRemoteDataIfNeeded(path, id) }
    }

    /**
     * Observes local data and sends it to the channel.
     *
     * @param path The path in the repository where the item should be retrieved from.
     * @param id The ID of the item to be retrieved.
     */
    private suspend fun ProducerScope<Result<Model>>.observeLocalData(path: String, id: String) {
        config.queryStrategies.byIdStrategy.apply {
            this@apply
                ?.setId(id)
                ?.execute()
                ?.collect { entity ->
                    entity?.let {
                        val model = config.modelMapper.toModel(it)
                        send(Result.success(model))
                        config.syncManager.queueOperation(
                            createSyncOperation(SyncOperation.SyncOperationType.SYNC, path, listOf(model), System.currentTimeMillis())
                        )
                    }
                }
        }
    }

    /**
     * Fetches remote data if it is not already present in the local database.
     *
     * @param path The path in the repository where the item should be retrieved from.
     * @param id The ID of the item to be retrieved.
     */
    private suspend fun ProducerScope<Result<Model>>.fetchRemoteDataIfNeeded(path: String, id: String) {
        val localEntity = config.queryStrategies.byIdStrategy.run {
            this@run?.setId(id)
            this@run?.execute()?.first()
        }

        if (localEntity == null) try {
            config.remoteDao.get(path, id).collect { result ->
                result
                    .onSuccess { model ->
                        send(Result.success(model))
                        config.syncManager.queueOperation(
                            createSyncOperation(SyncOperation.SyncOperationType.SYNC, path, listOf(model), System.currentTimeMillis())
                        )
                    }.onFailure { error ->
                        send(Result.failure(error))
                    }
            }
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    /**
     * Deletes an item from the repository.
     *
     * @param path The path in the repository where the item should be deleted from.
     * @param item The model to be deleted.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun delete(path: String, item: Model, timestamp: Long): Result<Unit> = config.runCatching {
        localDao.delete(path, modelMapper.toEntity(item), timestamp)
        syncManager.queueOperation(createSyncOperation(SyncOperation.SyncOperationType.DELETE, path, listOf(item), timestamp))
    }

    /**
     * Creates a sync operation for the given type, path, and items.
     *
     * @param type The type of the sync operation.
     * @param path The path of the operation.
     * @param items The items to be synced.
     * @param timestamp The timestamp to associate with the operation.
     * @return The created sync operation.
     */
    private fun createSyncOperation(type: SyncOperation.SyncOperationType, path: String, items: List<Model>, timestamp: Long) =
        SyncOperation(type, path, items, timestamp)
}