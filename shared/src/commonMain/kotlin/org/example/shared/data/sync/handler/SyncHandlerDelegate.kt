package org.example.shared.data.sync.handler

import kotlinx.coroutines.flow.first
import org.example.shared.data.local.dao.LocalDao
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.dao.ExtendedDao
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation

/**
 * Delegate class for handling synchronization of data models between local storage and remote data source.
 *
 * @param Model The type of the model to be synchronized.
 * @param Entity The type of the entity in the local database.
 * @property remoteDao The remote data access object for the model.
 * @property localDao The local data access object for the entity.
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

        if (type == SyncOperation.SyncOperationType.INSERT_ALL ||
            type == SyncOperation.SyncOperationType.UPDATE_ALL ||
            type == SyncOperation.SyncOperationType.DELETE_ALL
        ) {
            require(remoteDao is ExtendedDao<Model>) {
                "RemoteDao must be an instance of ExtendedDao"
            }
        }

        when (type) {
            SyncOperation.SyncOperationType.INSERT -> remoteDao.insert(path, data.first(), timestamp).getOrThrow()

            SyncOperation.SyncOperationType.UPDATE -> remoteDao.update(path, data.first(), timestamp).getOrThrow()

            SyncOperation.SyncOperationType.DELETE -> remoteDao.delete(path, data.first(), timestamp).getOrThrow()

            SyncOperation.SyncOperationType.SYNC -> data.forEach { sync(path, it.id) }

            SyncOperation.SyncOperationType.INSERT_ALL -> (remoteDao as ExtendedDao<Model>).insertAll(path, data, timestamp)
                .getOrThrow()

            SyncOperation.SyncOperationType.UPDATE_ALL -> (remoteDao as ExtendedDao<Model>).updateAll(path, data, timestamp)
                .getOrThrow()

            SyncOperation.SyncOperationType.DELETE_ALL -> (remoteDao as ExtendedDao<Model>).deleteAll(path, data, timestamp)
                .getOrThrow()
        }
    }

    /**
     * Synchronizes the data between the local and remote data sources.
     *
     * @param path The path of the data to be synchronized.
     * @param id The ID of the data to be synchronized.
     */
    private suspend fun sync(path: String, id: String) {
        val remote = remoteDao.get(path, id).first().getOrNull()
        val local = getStrategy.setId(id).execute().first()

        if (remote != null && local != null) {
            if (remote.lastUpdated > local.lastUpdated) {
                localDao.update(path, modelMapper.toEntity(remote, extractParentId(path)), remote.lastUpdated)
            } else {
                remoteDao.update(path, modelMapper.toModel(local), local.lastUpdated)
            }
        } else if (remote != null) {
            localDao.insert(path, modelMapper.toEntity(remote, extractParentId(path)), remote.lastUpdated)
        } else if (local != null) {
            remoteDao.insert(path, modelMapper.toModel(local), local.lastUpdated)
        }
    }

    /**
     * Extracts the parent ID from the given path.
     *
     * @param path The path to extract the parent ID from.
     * @return The extracted parent ID or null if not found.
     */
    private fun extractParentId(path: String) = path.split("/").run {
        if (size < 3) null else get(size - 2)
    }
}