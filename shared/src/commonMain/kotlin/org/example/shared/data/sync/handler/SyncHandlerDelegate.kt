package org.example.shared.data.sync.handler

import org.example.shared.data.local.dao.BaseDao
import org.example.shared.data.repository.util.ModelMapper
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.contract.DatabaseRecord
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation

/**
 * Delegate class for handling synchronization of data models between local storage and remote data source.
 *
 * @param Model The type of the model to be synchronized.
 * @param Entity The type of the entity in the local database.
 */
class SyncHandlerDelegate<Model : DatabaseRecord, Entity : DatabaseRecord>(
    private val remoteDataSource: RemoteDataSource<Model>,
    private val dao: BaseDao<Entity>,
    private val modelMapper: ModelMapper<Model, Entity>,
) : SyncHandler<Model> {
    /**
     * Handles the synchronization operation based on the type provided.
     *
     * @param operation The synchronization operation to be handled.
     */
    override suspend fun handleSync(operation: SyncOperation<Model>) = with(operation) {
        when (type) {
            SyncOperationType.CREATE -> remoteDataSource.create(path, data).getOrThrow()

            SyncOperationType.UPDATE -> remoteDataSource.update(path, data).getOrThrow()

            SyncOperationType.DELETE -> remoteDataSource.delete(path, data.id).getOrThrow()

            SyncOperationType.SYNC   -> operation.data.sync(path)
        }
    }

    /**
     * Synchronizes the model data with the remote data source.
     */
    private suspend fun Model.sync(path: String) {
        val remote = remoteDataSource.fetch(path, id).getOrThrow()

        if (lastUpdated < remote.lastUpdated) {
            dao.update(modelMapper.toEntity(remote))
        } else {
            remoteDataSource.create(path, this).getOrThrow()
        }
    }
}