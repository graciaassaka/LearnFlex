package org.example.shared.data.sync.handler

import kotlinx.coroutines.flow.first
import org.example.shared.data.local.dao.LocalDao
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.dao.ExtendedRemoteDao
import org.example.shared.domain.dao.RemoteDao
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation

/**
 * Delegate class for handling synchronization of data models between local storage and remote data source.
 *
 * @param Model The type of the model to be synchronized.
 * @param Entity The type of the entity in the local database.
 */
class SyncHandlerDelegate<Model : DatabaseRecord, Entity : DatabaseRecord>(
    private val remoteDao: RemoteDao<Model>,
    private val localDao: LocalDao<Entity>,
    private val modelMapper: ModelMapper<Model, Entity>,
) : SyncHandler<Model> {
    /**
     * Handles the synchronization operation based on the type provided.
     *
     * @param operation The synchronization operation to be handled.
     */
    override suspend fun handleSync(operation: SyncOperation<Model>) = with(operation) {
        require(data.isNotEmpty()) { "Data must not be empty" }

        if (type == SyncOperationType.INSERT_ALL ||
            type == SyncOperationType.UPDATE_ALL ||
            type == SyncOperationType.DELETE_ALL
        ) {
            require(remoteDao is ExtendedRemoteDao<Model>) {
                "RemoteDao must be an instance of ExtendedRemoteDao"
            }
        }

        when (type) {
            SyncOperationType.INSERT     -> remoteDao.insert(path, data.first()).getOrThrow()

            SyncOperationType.UPDATE     -> remoteDao.update(path, data.first()).getOrThrow()

            SyncOperationType.DELETE     -> remoteDao.delete(path, data.first()).getOrThrow()

            SyncOperationType.SYNC       -> data.first().sync(path)

            SyncOperationType.INSERT_ALL -> (remoteDao as ExtendedRemoteDao<Model>).insertAll(path, data).getOrThrow()

            SyncOperationType.UPDATE_ALL -> (remoteDao as ExtendedRemoteDao<Model>).updateAll(path, data).getOrThrow()

            SyncOperationType.DELETE_ALL -> (remoteDao as ExtendedRemoteDao<Model>).deleteAll(path, data).getOrThrow()
        }
    }

    /**
     * Synchronizes the model data with the remote data source.
     */
    private suspend fun Model.sync(path: String) {
        val remote = remoteDao.get(path, id).first().getOrThrow()

        if (lastUpdated < remote.lastUpdated) {
            localDao.update(modelMapper.toEntity(remote))
        } else {
            remoteDao.insert(path, this).getOrThrow()
        }
    }
}