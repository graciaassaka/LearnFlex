package org.example.shared.data.repository.util

import org.example.shared.data.local.dao.LocalDao
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.domain.constant.DataCollection
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncManager

/**
 * Configuration class for repository operations.
 *
 * @param Model The type of the database record.
 * @param Entity The type of the room entity.
 * @property dataCollection The data collection for the model.
 * @property remoteDao The remote data access object for the model.
 * @property localDao The local data access object for the entity.
 * @property modelMapper The mapper to convert between model and entity.
 * @property syncManager The manager to handle synchronization of the model.
 * @property queryStrategies The strategies for querying the local database.
 */
data class RepositoryConfig<Model : DatabaseRecord, Entity : RoomEntity>(
    val dataCollection: DataCollection,
    val remoteDao: Dao<Model>,
    val localDao: LocalDao<Entity>,
    val modelMapper: ModelMapper<Model, Entity>,
    val syncManager: SyncManager<Model>,
    val queryStrategies: QueryStrategies<Entity> = QueryStrategies()
)