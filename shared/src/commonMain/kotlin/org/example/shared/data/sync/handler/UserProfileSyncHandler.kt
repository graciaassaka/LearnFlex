package org.example.shared.data.sync.handler

import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.data_source.UserProfileRemoteDataSource
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation

class UserProfileSyncHandler(
    private val remoteDataSource: UserProfileRemoteDataSource,
    private val userProfileDao: UserProfileDao
) : SyncHandler<UserProfile> {
    override suspend fun handleSync(operation: SyncOperation<UserProfile>) {
        when (operation.type) {
            SyncOperationType.CREATE,
            SyncOperationType.UPDATE -> remoteDataSource.setUserProfile(operation.data).getOrThrow()

            SyncOperationType.DELETE -> remoteDataSource.deleteUserProfile(operation.data.id).getOrThrow()

            SyncOperationType.SYNC   -> syncUserProfile(operation)
        }
    }

    private suspend fun syncUserProfile(operation: SyncOperation<UserProfile>) {
        val remote = remoteDataSource.fetchUserProfile(operation.data.id).getOrThrow()

        if (operation.data.lastUpdated < remote.lastUpdated) {
            userProfileDao.update(UserProfileEntity.fromUserProfile(remote))
        } else {
            remoteDataSource.setUserProfile(operation.data).getOrThrow()
        }
    }
}

