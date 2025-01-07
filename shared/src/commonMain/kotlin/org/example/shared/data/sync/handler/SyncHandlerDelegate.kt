package org.example.shared.data.sync.handler

import kotlinx.coroutines.flow.first
import org.example.shared.data.local.dao.LocalDao
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.dao.ExtendedDao
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.storage_operations.util.Path
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation

/**
 * Delegate class for handling synchronization of data models between local storage and remote data source.
 *
 * @param Model The type of the model to be synchronized.
 * @param Entity The type of the entity in the local database.
 * @property remoteDao The remote data access object for the model.
 * @property localDao The local data access object for the entity.
 * @property getStrategy The query strategy to get a single entity from the local database.
 * @property modelMapper The mapper to convert between model and entity.
 */
class SyncHandlerDelegate<Model : DatabaseRecord, Entity : RoomEntity>(
    private val remoteDao: Dao<Model>,
    private val localDao: LocalDao<Entity>,
    private val getStrategy: QueryStrategies.SingleEntityStrategyHolder<Entity>,
    private val modelMapper: ModelMapper<Model, Entity>,
) : SyncHandler<Model> {
    /**
     * Handles the synchronization operation based on the type provided.
     *
     * @param operation The synchronization operation to be handled.
     */
    override suspend fun handleSync(operation: SyncOperation<Model>) = with(operation) {
        require(data.isNotEmpty()) { "Data must not be empty" }

        if (type == SyncOperation.Type.INSERT_ALL ||
            type == SyncOperation.Type.UPDATE_ALL ||
            type == SyncOperation.Type.DELETE_ALL
        ) {
            require(remoteDao is ExtendedDao<Model>) {
                "RemoteDao must be an instance of ExtendedDao"
            }
        }

        when (type) {
            SyncOperation.Type.INSERT -> remoteDao.insert(data.first(), path, timestamp).getOrThrow()

            SyncOperation.Type.UPDATE -> remoteDao.update(data.first(), path, timestamp).getOrThrow()

            SyncOperation.Type.DELETE -> remoteDao.delete(data.first(), path, timestamp).getOrThrow()

            SyncOperation.Type.SYNC -> data.forEach { sync(path, it.id) }

            SyncOperation.Type.INSERT_ALL -> (remoteDao as ExtendedDao<Model>).insertAll(data, path, timestamp)
                .getOrThrow()

            SyncOperation.Type.UPDATE_ALL -> (remoteDao as ExtendedDao<Model>).updateAll(data, path, timestamp)
                .getOrThrow()

            SyncOperation.Type.DELETE_ALL -> (remoteDao as ExtendedDao<Model>).deleteAll(data, path, timestamp)
                .getOrThrow()
        }
    }

    /**
     * Synchronizes the data between the local and remote data sources.
     *
     * @param path The path of the data to be synchronized.
     * @param id The ID of the data to be synchronized.
     */
    private suspend fun sync(path: Path, id: String) {
        val remote = remoteDao.get(path).first().getOrNull()
        val local = getStrategy.setId(id).execute().first()

        if (remote != null && local != null) {
            if (remote.lastUpdated > local.lastUpdated) {
                localDao.update(path, modelMapper.toEntity(remote, path.getParentId()), remote.lastUpdated)
            } else {
                remoteDao.update(modelMapper.toModel(local), path, local.lastUpdated)
            }
        } else if (remote != null) {
            localDao.insert(path, modelMapper.toEntity(remote, path.getParentId()), remote.lastUpdated)
        } else if (local != null) {
            remoteDao.insert(modelMapper.toModel(local), path, local.lastUpdated)
        }
    }
}