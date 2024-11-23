package org.example.shared.data.repository

import kotlinx.coroutines.flow.flow
import org.example.shared.data.local.dao.contract.BaseDao
import org.example.shared.data.repository.util.ModelMapper
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.contract.DatabaseRecord
import org.example.shared.domain.repository.Repository
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.sync.SyncOperation

/**
 * BaseRepository is an abstract class that provides basic CRUD operations and synchronization
 * management for a given Model and Entity.
 *
 * @param Model The type of the model.
 * @param Entity The type of the entity.
 * @param remoteDataSource The remote data source for fetching models.
 * @param dao The data access object for performing local database operations.
 * @param syncManager The manager for handling synchronization operations.
 * @param syncOperationFactory A factory function for creating sync operations.
 * @param modelMapper A mapper for converting between models and entities.
 */
abstract class RepositoryImpl<Model : DatabaseRecord, Entity>(
    private val remoteDataSource: RemoteDataSource<Model>,
    private val dao: BaseDao<Entity>,
    private val syncManager: SyncManager<Model>,
    private val syncOperationFactory: (SyncOperationType, Model) -> SyncOperation<Model>,
    private val modelMapper: ModelMapper<Model, Entity>
) : Repository<Model> {
    /**
     * Creates a new item in the repository.
     *
     * @param item The model to be created.
     * @return A Result indicating success or failure.
     */
    override suspend fun create(item: Model) = runCatching {
        dao.insert(modelMapper.toEntity(item))
        syncManager.queueOperation(syncOperationFactory(SyncOperationType.CREATE, item))
    }

    /**
     * Updates an existing item in the repository.
     *
     * @param item The model to be updated.
     * @return A Result indicating success or failure.
     */
    override suspend fun update(item: Model) = runCatching {
        dao.update(modelMapper.toEntity(item))
        syncManager.queueOperation(syncOperationFactory(SyncOperationType.UPDATE, item))
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param id The ID of the item to be retrieved.
     * @return A flow emitting the Result containing the model or an error.
     */
    override fun get(id: String) = flow {
        try {
            val entity = dao.get(id)

            if (entity != null) {
                val model = modelMapper.toModel(entity)
                syncManager.queueOperation(syncOperationFactory(SyncOperationType.SYNC, model))
                emit(Result.success(model))
            } else {
                val remoteModel = remoteDataSource.fetch(id).getOrThrow()
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
     * @param item The model to be deleted.
     * @return A Result indicating success or failure.
     */
    override suspend fun delete(item: Model) = runCatching {
        dao.delete(modelMapper.toEntity(item))
        syncManager.queueOperation(syncOperationFactory(SyncOperationType.DELETE, item))
    }
}