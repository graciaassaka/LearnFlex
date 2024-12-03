package org.example.shared.data.repository.component

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.model.definition.DatabaseRecord
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
     * @return A [Result] indicating success or failure.
     */
    override suspend fun insert(path: String, item: Model): Result<Unit> = config.runCatching {
        localDao.insert(modelMapper.toEntity(item))
        syncManager.queueOperation(createSyncOperation(SyncOperationType.INSERT, path, listOf(item)))
    }

    /**
     * Updates an existing item in the repository.
     *
     * @param path The path in the repository where the item should be updated.
     * @param item The model to be updated.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun update(path: String, item: Model): Result<Unit> = config.runCatching {
        localDao.update(modelMapper.toEntity(item))
        syncManager.queueOperation(createSyncOperation(SyncOperationType.UPDATE, path, listOf(item)))
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
        config.queryStrategies.getByIdStrategy().apply {
            this@apply.setId(id)

            this@apply.execute().collect { entity ->
                entity?.let {
                    val model = config.modelMapper.toModel(it)
                    send(Result.success(model))
                    config.syncManager.queueOperation(
                        createSyncOperation(SyncOperationType.SYNC, path, listOf(model))
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
        val localEntity = config.queryStrategies.getByIdStrategy().run {
            this@run.setId(id)
            this@run.execute().first()
        }

        if (localEntity == null) try {
            config.remoteDao.get(path, id).collect { result ->
                result
                    .onSuccess { model ->
                        config.localDao.insert(config.modelMapper.toEntity(model))
                        send(Result.success(model))
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
     * @return A [Result] indicating success or failure.
     */
    override suspend fun delete(path: String, item: Model): Result<Unit> = config.runCatching {
        localDao.delete(modelMapper.toEntity(item))
        syncManager.queueOperation(createSyncOperation(SyncOperationType.DELETE, path, listOf(item)))
    }

    /**
     * Creates a sync operation for the given type, path, and items.
     *
     * @param type The type of the sync operation.
     * @param path The path of the operation.
     * @param items The items to be synced.
     * @return The created sync operation.
     */
    private fun createSyncOperation(type: SyncOperationType, path: String, items: List<Model>) =
        SyncOperation(type, path, items)
}