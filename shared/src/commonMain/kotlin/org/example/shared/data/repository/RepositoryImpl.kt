package org.example.shared.data.repository

import kotlinx.coroutines.flow.flow
import org.example.shared.data.local.dao.BaseDao
import org.example.shared.data.repository.util.ModelMapper
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.contract.DatabaseRecord
import org.example.shared.domain.repository.Repository
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.sync.SyncOperation

/**
 * RepositoryImpl is an abstract class that provides basic CRUD operations and synchronization
 * management for a given Model and Entity.
 *
 * @param Model The type of the model, which must implement [DatabaseRecord].
 * @param Entity The type of the entity.
 * @param remoteDataSource The remote data source for fetching models.
 * @param dao The data access object for performing local database operations.
 * @param getStrategy A strategy function for retrieving an entity by its ID.
 * @param syncManager The manager for handling synchronization operations.
 * @param syncOperationFactory A factory function for creating sync operations.
 * @param modelMapper A mapper for converting between models and entities.
 */
abstract class RepositoryImpl<Model : DatabaseRecord, Entity>(
    private val remoteDataSource: RemoteDataSource<Model>,
    private val dao: BaseDao<Entity>,
    private val getStrategy: suspend (String) -> Entity?,
    private val syncManager: SyncManager<Model>,
    private val syncOperationFactory: (SyncOperationType, String, Model) -> SyncOperation<Model>,
    private val modelMapper: ModelMapper<Model, Entity>
) : Repository<Model> {

    /**
     * Creates a new item in the repository.
     *
     * @param path The path in the repository where the item should be created.
     * @param item The model to be created.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun create(path: String, item: Model): Result<Unit> = runCatching {
        dao.insert(modelMapper.toEntity(item))
        syncManager.queueOperation(syncOperationFactory(SyncOperationType.CREATE, path, item))
    }

    /**
     * Updates an existing item in the repository.
     *
     * @param path The path in the repository where the item should be updated.
     * @param item The model to be updated.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun update(path: String, item: Model): Result<Unit> = runCatching {
        dao.update(modelMapper.toEntity(item))
        syncManager.queueOperation(syncOperationFactory(SyncOperationType.UPDATE, path, item))
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param path The path in the repository where the item should be retrieved from.
     * @param id The ID of the item to be retrieved.
     * @return A [Flow] emitting the [Result] containing the model or an error.
     */
    override fun get(path: String, id: String) = flow {
        try {
            val entity = getStrategy(id)

            if (entity != null) {
                val model = modelMapper.toModel(entity)
                syncManager.queueOperation(syncOperationFactory(SyncOperationType.SYNC, path, model))
                emit(Result.success(model))
            } else {
                val remoteModel = remoteDataSource.fetch(path, id).getOrThrow()
                dao.insert(modelMapper.toEntity(remoteModel))
                emit(Result.success(remoteModel))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Deletes an item from the repository.
     *
     * @param path The path in the repository where the item should be deleted from.
     * @param item The model to be deleted.
     * @return A [Result] indicating success or failure.
     */
    override suspend fun delete(path: String, item: Model): Result<Unit> = runCatching {
        dao.delete(modelMapper.toEntity(item))
        syncManager.queueOperation(syncOperationFactory(SyncOperationType.DELETE, path, item))
    }
}