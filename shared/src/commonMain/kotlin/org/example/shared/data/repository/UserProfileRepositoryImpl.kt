package org.example.shared.data.repository

import kotlinx.coroutines.flow.flow
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.data.sync.operation.UserProfileSyncOperation
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.data_source.UserProfileRemoteDataSource
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepository
import org.example.shared.domain.sync.SyncManager

/**
 * Implementation of [UserProfileRepository] that handles user profile operations
 * with local database caching and synchronization with a remote data source.
 *
 * @property remoteDataSource The remote data source for user profile operations.
 * @property userProfileDao The DAO for accessing user profile data in the local database.
 * @property syncManager The manager that handles synchronization operations.
 */
class UserProfileRepositoryImpl(
    private val remoteDataSource: UserProfileRemoteDataSource,
    private val userProfileDao: UserProfileDao,
    private val syncManager: SyncManager<UserProfile>
) : UserProfileRepository {
    /**
     * Creates a new user profile in the local database and queues a CREATE sync operation.
     *
     * @param userProfile The user profile to be created.
     */
    override suspend fun createUserProfile(userProfile: UserProfile) = runCatching {
        userProfileDao.insert(UserProfileEntity.fromUserProfile(userProfile))
        syncManager.queueOperation(UserProfileSyncOperation(SyncOperationType.CREATE, userProfile))
    }

    /**
     * Updates an existing user profile in the local database and queues an update operation for synchronization.
     *
     * @param userProfile The user profile to update.
     */
    override suspend fun updateUserProfile(userProfile: UserProfile) = runCatching {
        userProfileDao.insert(UserProfileEntity.fromUserProfile(userProfile))
        syncManager.queueOperation(UserProfileSyncOperation(SyncOperationType.UPDATE, userProfile))
    }

    /**
     * Retrieves the user profile for the given user ID. If a profile exists locally, it emits it
     * after synchronizing. Otherwise, it fetches the profile from a remote source, stores it locally,
     * and then emits it.
     *
     * @param id The ID of the user profile to retrieve.
     * @return A flow emitting a Result containing the user profile or an error on failure.
     */
    override fun getUserProfile(id: String) = flow {
        try {
            val userProfileEntity = userProfileDao.getActiveProfile()

            if (userProfileEntity != null) {
                syncManager.queueOperation(UserProfileSyncOperation(SyncOperationType.SYNC, userProfileEntity))
                emit(Result.success(userProfileEntity.toUserProfile()))
            } else {
                val remoteUserProfile = remoteDataSource.fetchUserProfile(id).getOrThrow()
                userProfileDao.insert(UserProfileEntity.fromUserProfile(remoteUserProfile))
                emit(Result.success(remoteUserProfile))
            }

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Deletes a user profile from the local database and queues a delete operation
     * to be synced with the remote data source.
     *
     * @param userProfile The user profile to be deleted.
     */
    override suspend fun deleteUserProfile(userProfile: UserProfile) = runCatching {
        userProfileDao.delete(UserProfileEntity.fromUserProfile(userProfile))
        syncManager.queueOperation(UserProfileSyncOperation(SyncOperationType.DELETE, userProfile))
    }
}