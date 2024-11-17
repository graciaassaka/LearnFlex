package org.example.shared.domain.sync

import kotlinx.coroutines.flow.StateFlow
import org.example.shared.domain.constant.SyncStatus

interface SyncManager<T> {
    val syncStatus: StateFlow<SyncStatus>
    suspend fun queueOperation(operation: SyncOperation<T>)
}