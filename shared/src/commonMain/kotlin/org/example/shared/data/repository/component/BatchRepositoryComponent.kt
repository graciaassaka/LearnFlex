package org.example.shared.data.repository.component

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.example.shared.data.local.dao.ExtendedLocalDao
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.dao.ExtendedDao
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.repository.util.QueryStrategy
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.util.Path
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
     * @param timestamp The timestamp to associate with the operation.
     * @return A Result indicating success or failure.
     */
    override suspend fun insertAll(items: List<Model>, path: Path, timestamp: Long): Result<Unit> =
        config.runCatching {
            require(path.isCollectionPath())
            require(localDao is ExtendedLocalDao)
            localDao.insertAll(
                path = path,
                items = items.map { model -> modelMapper.toEntity(model, path.getParentId()) },
                timestamp = timestamp
            )
            syncManager.queueOperation(SyncOperation(SyncOperation.Type.INSERT_ALL, path, items, timestamp))
        }

    /**
     * Updates all items in the local database and queues a sync operation.
     *
     * @param path The path for the operation.
     * @param items The list of items to update.
     * @param timestamp The timestamp to associate with the operation.
     * @return A Result indicating success or failure.
     */
    override suspend fun updateAll(items: List<Model>, path: Path, timestamp: Long): Result<Unit> =
        config.runCatching {
            require(path.isCollectionPath())
            require(localDao is ExtendedLocalDao)
            localDao.updateAll(
                path = path,
                items = items.map { model -> modelMapper.toEntity(model, path.getParentId()) },
                timestamp = timestamp
            )
            syncManager.queueOperation(SyncOperation(SyncOperation.Type.UPDATE_ALL, path, items, timestamp))
        }

    /**
     * Deletes all items from the local database and queues a sync operation.
     *
     * @param path The path for the operation.
     * @param items The list of items to delete.
     * @param timestamp The timestamp to associate with the operation.
     * @return A Result indicating success or failure.
     */
    override suspend fun deleteAll(items: List<Model>, path: Path, timestamp: Long): Result<Unit> =
        config.runCatching {
            require(path.isCollectionPath())
            require(localDao is ExtendedLocalDao)
            localDao.deleteAll(
                path = path,
                items = items.map { model -> modelMapper.toEntity(model, path.getParentId()) },
                timestamp = timestamp
            )
            syncManager.queueOperation(SyncOperation(SyncOperation.Type.DELETE_ALL, path, items, timestamp))
        }

    /**
     * Retrieves all items as a Flow from the local database and syncs with remote data.
     *
     * @param path The path for the operation.
     * @return A Flow emitting the result of the operation.
     */
    override fun getAll(path: Path): Flow<Result<List<Model>>> = channelFlow {
        require(path.isCollectionPath())
        require(config.remoteDao is ExtendedDao)
        require(config.localDao is ExtendedLocalDao)

        var lastEmittedData: List<Model>? = null

        launch {
            observeLocalData(
                path = path,
                lastEmitted = lastEmittedData,
                queryStrategy = config.queryStrategies.byParentStrategy!!.setParentId(path.getParentId()!!)
            ) {
                lastEmittedData = it
            }
        }
        launch {
            fetchAndSyncRemoteData(
                path = path,
                lastEmitted = lastEmittedData,
                remoteDataFlow = config.remoteDao.getAll(path)
            ) {
                lastEmittedData = it
            }
        }
    }.distinctUntilChanged { old, new ->
        if (old.isSuccess && new.isSuccess) old.getOrNull() == new.getOrNull() else false
    }.catch {
        emit(Result.failure(it))
    }

    /**
     * Observes local data changes and emits the result.
     *
     * @param lastEmitted The last emitted data.
     * @param queryStrategy The query strategy to use.
     * @param onEmit The callback to invoke when new data is emitted.
     */
    private suspend fun ProducerScope<Result<List<Model>>>.observeLocalData(
        path: Path,
        lastEmitted: List<Model>?,
        queryStrategy: QueryStrategy<List<Entity>>,
        onEmit: (List<Model>) -> Unit
    ) = try {
        queryStrategy.apply {
            this@apply.execute().collect { entities ->
                entities
                    ?.map(config.modelMapper::toModel)
                    ?.let { models ->
                        if (models != lastEmitted) {
                            send(Result.success(models))
                            if (models.isNotEmpty()) {
                                config.syncManager.queueOperation(SyncOperation(SyncOperation.Type.SYNC, path, models))
                            }
                            onEmit(models)
                        }
                    }
            }
        }
    } catch (e: Exception) {
        send(Result.failure(e))
    }

    /**
     * Fetches remote data, syncs it with the local database, and emits the result.
     *
     * @param path The path for the operation.
     * @param lastEmitted The last emitted data.
     * @param onEmit The callback to invoke when new data is emitted.
     */
    private suspend fun ProducerScope<Result<List<Model>>>.fetchAndSyncRemoteData(
        path: Path,
        lastEmitted: List<Model>?,
        remoteDataFlow: Flow<Result<List<Model>>>,
        onEmit: (List<Model>) -> Unit
    ) = try {
        remoteDataFlow.collect { result ->
            result.onSuccess { models ->
                if (models != lastEmitted) {
                    send(Result.success(models))
                    if (models.isNotEmpty()) {
                        config.syncManager.queueOperation(SyncOperation(SyncOperation.Type.SYNC, path, models))
                    }
                    onEmit(models)
                }
            }.onFailure { error ->
                send(Result.failure(error))
            }
        }
    } catch (e: Exception) {
        send(Result.failure(e))
    }
}