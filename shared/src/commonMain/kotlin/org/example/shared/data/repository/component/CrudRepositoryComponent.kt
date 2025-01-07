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
import org.example.shared.domain.storage_operations.util.Path
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
    override suspend fun insert(item: Model, path: Path, timestamp: Long): Result<Unit> = config.runCatching {
        require(path.isDocumentPath()) { "Path must point to a document" }
        localDao.insert(path, modelMapper.toEntity(item, path.getParentId()), timestamp)
        syncManager.queueOperation(SyncOperation(SyncOperation.Type.INSERT, path, listOf(item), timestamp))
    }

    /**
     * Updates an existing item in the repository.
     *
     * @param path The path in the repository where the item should be updated.
     * @param item The model to be updated.
     * @param timestamp The timestamp to associate with the operation.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun update(item: Model, path: Path, timestamp: Long): Result<Unit> = config.runCatching {
        require(path.isDocumentPath()) { "Path must point to a document" }
        localDao.update(path, modelMapper.toEntity(item, path.getParentId()), timestamp)
        syncManager.queueOperation(SyncOperation(SyncOperation.Type.UPDATE, path, listOf(item), timestamp))
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param path The path in the repository where the item should be retrieved from.
     * @return A [Flow] emitting the [Result] containing the model or an error.
     */
    override fun get(path: Path) = channelFlow {
        require(path.isDocumentPath()) { "Path must point to a document" }
        launch { observeLocalData(path) }
        launch { fetchRemoteDataIfNeeded(path) }
    }

    /**
     * Observes local data and sends it to the channel.
     *
     * @param path The path in the repository where the item should be retrieved from.
     */
    private suspend fun ProducerScope<Result<Model>>.observeLocalData(path: Path) {
        config.queryStrategies.byIdStrategy?.apply {
            setId(path.getId())
                .execute()
                .collect { entity ->
                    if (entity != null) config.modelMapper.toModel(entity).let {
                        send(Result.success(it))
                        config.syncManager.queueOperation(SyncOperation(SyncOperation.Type.SYNC, path, listOf(it)))
                    }
                }
        }
    }

    /**
     * Fetches remote data if it is not already present in the local database.
     *
     * @param path The path in the repository where the item should be retrieved from.
     */
    private suspend fun ProducerScope<Result<Model>>.fetchRemoteDataIfNeeded(path: Path) {
        val localEntity = config.queryStrategies.byIdStrategy?.run { setId(path.getId()).execute().first() }

        if (localEntity == null) try {
            config.remoteDao.get(path).collect { result ->
                result.onSuccess { model ->
                    send(Result.success(model))
                    config.syncManager.queueOperation(SyncOperation(SyncOperation.Type.SYNC, path, listOf(model)))
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
    override suspend fun delete(item: Model, path: Path, timestamp: Long): Result<Unit> = config.runCatching {
        localDao.delete(path, modelMapper.toEntity(item, path.getParentId()), timestamp)
        syncManager.queueOperation(SyncOperation(SyncOperation.Type.DELETE, path, listOf(item), timestamp))
    }
}