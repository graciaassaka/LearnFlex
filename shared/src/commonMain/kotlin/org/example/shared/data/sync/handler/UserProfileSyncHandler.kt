package org.example.shared.data.sync.handler

import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.data.sync.handler.delagate.SyncDelegate
import org.example.shared.data.sync.util.ModelHelper
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.sync.SyncHandler

class UserProfileSyncHandler(
    remoteDataSource: RemoteDataSource<UserProfile>,
    userProfileDao: UserProfileDao
) : SyncHandler<UserProfile> by SyncDelegate(
    remoteDataSource,
    userProfileDao,
    object : ModelHelper<UserProfile, UserProfileEntity> {
        override fun getId(model: UserProfile) = model.id
        override fun getLastUpdated(model: UserProfile) = model.lastUpdated
        override fun toEntity(model: UserProfile) = with(model) {
            UserProfileEntity(id, username, email, photoUrl, preferences, createdAt, lastUpdated)
        }
    }
)

