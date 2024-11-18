package org.example.shared.data.repository

import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.data.repository.util.ModelMapper
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.sync.SyncOperation

/**
 * Implementation of [Repository] that handles user profile operations
 * with local database caching and synchronization with a remote data source.
 *
 * @property remoteDataSource The remote data source for user profile operations.
 * @property userProfileDao The DAO for accessing user profile data in the local database.
 * @property syncManager The manager that handles synchronization operations.
 */
class UserRepository(
    remoteDataSource: RemoteDataSource<UserProfile>,
    userProfileDao: UserProfileDao,
    syncManager: SyncManager<UserProfile>
) : BaseRepository<UserProfile, UserProfileEntity>(
    remoteDataSource = remoteDataSource,
    dao = userProfileDao,
    syncManager = syncManager,
    syncOperationFactory = { type, profile -> SyncOperation(type, profile) },
    modelMapper = object : ModelMapper<UserProfile, UserProfileEntity> {
        override fun toModel(entity: UserProfileEntity) = with(entity) {
            UserProfile(id, username, email, photoUrl, preferences, createdAt, lastUpdated)
        }

        override fun toEntity(model: UserProfile) = with(model) {
            UserProfileEntity(id, username, email, photoUrl, preferences, createdAt, lastUpdated)
        }
    }
)