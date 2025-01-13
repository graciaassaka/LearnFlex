package org.example.shared.data.repository.component

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import org.example.shared.data.local.dao.ExtendedLocalDao
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.dao.ExtendedDao
import org.example.shared.domain.model.interfaces.DatabaseRecord
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
    override fun getAll(path: Path): Flow<Result<List<Model>>> {
        var remote: List<Model>? = null
        var local: List<Model>? = null

        return channelFlow {
            require(path.isCollectionPath())
            require(config.remoteDao is ExtendedDao)
            require(config.localDao is ExtendedLocalDao)

            val remoteJob = async {
                config.remoteDao
                    .getAll(path)
                    .firstOrNull()
                    ?.getOrThrow()
            }
            val localJob = async {
                config.queryStrategies
                    .byParentStrategy!!
                    .setParentId(path.getParentId()!!)
                    .execute()
                    .firstOrNull { it.isNotEmpty() }
                    ?.map(config.modelMapper::toModel)
            }

            local = localJob.await()
            remote = remoteJob.await()
            send(Result.success(remote ?: local ?: emptyList()))
        }.catch {
            emit(Result.failure(it))
        }.onCompletion {
            remote?.let {
                config.syncManager.queueOperation(
                    SyncOperation(SyncOperation.Type.SYNC, path, it)
                )
            }
        }
    }
}