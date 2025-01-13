package org.example.shared.data.repository.component

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
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
    override fun get(path: Path): Flow<Result<Model>> {
        var remote: Model? = null
        var local: Model? = null

        return channelFlow {
            require(path.isDocumentPath())
            val remoteJob = async {
                config.remoteDao
                    .get(path)
                    .first()
                    .getOrThrow()
            }
            val localJob = async {
                config.queryStrategies.byIdStrategy
                    ?.setId(path.getId())
                    ?.execute()
                    ?.first()
                    ?.let(config.modelMapper::toModel)
            }

            local = localJob.await()
            remote = remoteJob.await()
            send(Result.success(local ?: remote))
        }.catch {
            emit(Result.failure(it))
        }.onCompletion {
            remote?.let {
                config.syncManager.queueOperation(
                    SyncOperation(SyncOperation.Type.SYNC, path, listOf(it))
                )
            }
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