package org.example.shared.data.repository.component

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.example.shared.data.local.dao.ExtendedLocalDao
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.dao.ExtendedRemoteDao
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.sync.SyncOperation

/**
 * A repository component for batch operations on models and entities.
 *
 * @param Model The type of the model.
 * @param Entity The type of the entity.
 * @param config The repository configuration.
 */
class BatchRepositoryComponent<Model : DatabaseRecord, Entity : RoomEntity>(
    private val config: RepositoryConfig<Model, Entity>
) : BatchOperations<Model> {

    /**
     * Inserts all items into the local database and queues a sync operation.
     *
     * @param path The path for the operation.
     * @param items The list of items to insert.
     * @return A Result indicating success or failure.
     */
    override suspend fun insertAll(path: String, items: List<Model>): Result<Unit> = config.runCatching {
        require(localDao is ExtendedLocalDao) { "LocalDao must implement ExtendedLocalDao to support batch operations" }
        localDao.insertAll(items.map { model ->
            modelMapper.toEntity(model, extractParentId(path))
        })
        syncManager.queueOperation(
            createSyncOperation(SyncOperationType.INSERT_ALL, path, items)
        )
    }

    /**
     * Updates all items in the local database and queues a sync operation.
     *
     * @param path The path for the operation.
     * @param items The list of items to update.
     * @return A Result indicating success or failure.
     */
    override suspend fun updateAll(path: String, items: List<Model>): Result<Unit> = config.runCatching {
        require(localDao is ExtendedLocalDao) { "LocalDao must implement ExtendedLocalDao to support batch operations" }
        localDao.updateAll(items.map { model ->
            modelMapper.toEntity(model, extractParentId(path))
        })
        syncManager.queueOperation(
            createSyncOperation(SyncOperationType.UPDATE_ALL, path, items)
        )
    }

    /**
     * Deletes all items from the local database and queues a sync operation.
     *
     * @param path The path for the operation.
     * @param items The list of items to delete.
     * @return A Result indicating success or failure.
     */
    override suspend fun deleteAll(path: String, items: List<Model>): Result<Unit> = config.runCatching {
        require(localDao is ExtendedLocalDao) { "LocalDao must implement ExtendedLocalDao to support batch operations" }
        localDao.deleteAll(items.map { model ->
            modelMapper.toEntity(model, extractParentId(path))
        })
        syncManager.queueOperation(
            createSyncOperation(SyncOperationType.DELETE_ALL, path, items)
        )
    }

    /**
     * Retrieves all items as a Flow from the local database and syncs with remote data.
     *
     * @param path The path for the operation.
     * @return A Flow emitting the result of the operation.
     */
    override fun getAll(path: String): Flow<Result<List<Model>>> = channelFlow {
        var lastEmittedData: List<Model>? = null

        launch {
            observeLocalData(path, lastEmittedData) { lastEmittedData = it }
        }
        launch {
            fetchAndSyncRemoteData(path, lastEmittedData) { lastEmittedData = it }
        }
    }.distinctUntilChanged { old, new ->
        when {
            old.isSuccess && new.isSuccess -> old.getOrNull() == new.getOrNull()
            else                           -> false
        }
    }

    /**
     * Observes local data changes and emits the result.
     *
     * @param path The path for the operation.
     * @param lastEmitted The last emitted data.
     * @param onEmit The callback to invoke when new data is emitted.
     */
    private suspend fun ProducerScope<Result<List<Model>>>.observeLocalData(
        path: String,
        lastEmitted: List<Model>?,
        onEmit: (List<Model>) -> Unit
    ) {
        config.queryStrategies.getAllStrategy().apply {
            this@apply.setParentId(extractParentId(path))
            this@apply.execute().collect { entities ->
                val models = entities.map { config.modelMapper.toModel(it) }
                if (models != lastEmitted) {
                    send(Result.success(models))
                    onEmit(models)
                }
            }
        }
    }

    /**
     * Fetches remote data, syncs it with the local database, and emits the result.
     *
     * @param path The path for the operation.
     * @param lastEmitted The last emitted data.
     * @param onEmit The callback to invoke when new data is emitted.
     */
    private suspend fun ProducerScope<Result<List<Model>>>.fetchAndSyncRemoteData(
        path: String,
        lastEmitted: List<Model>?,
        onEmit: (List<Model>) -> Unit
    ) = try {
        require(config.remoteDao is ExtendedRemoteDao) { "RemoteDao must implement ExtendedRemoteDao to support batch operations" }
        require(config.localDao is ExtendedLocalDao) { "LocalDao must implement ExtendedLocalDao to support batch operations" }

        config.remoteDao.getAll(path).collect { result ->
            result.onSuccess { models ->
                if (models != lastEmitted) {
                    config.localDao.upsertAll(models.map { config.modelMapper.toEntity(it, extractParentId(path)) })
                    send(Result.success(models))
                    onEmit(models)
                }
            }.onFailure { error ->
                send(Result.failure(error))
            }
        }
    } catch (e: Exception) {
        send(Result.failure(e))
    }

    /**
     * Extracts the parent ID from the given path.
     *
     * @param path The path to extract the parent ID from.
     * @return The extracted parent ID or null if not found.
     */
    private fun extractParentId(path: String) = path.split("/").run {
        if (size < 4) null else get(size - 3)
    }

    /**
     * Creates a sync operation for the given type, path, and items.
     *
     * @param type The type of the sync operation.
     * @param path The path for the operation.
     * @param items The list of items for the operation.
     * @return The created SyncOperation.
     */
    private fun createSyncOperation(type: SyncOperationType, path: String, items: List<Model>) =
        SyncOperation(type, path, items)
}