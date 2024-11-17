package org.example.shared.data.sync.operation

import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.sync.SyncOperation

data class UserProfileSyncOperation(
    override val type: SyncOperationType,
    override val data: UserProfile
): SyncOperation<UserProfile>