package org.example.shared.domain.sync

import kotlinx.coroutines.flow.StateFlow
import org.example.shared.domain.constant.SyncStatus

/**
 * Interface representing a manager for synchronizing operations.
 *
 * @param Model The type of the model being synchronized.
 */
interface SyncManager<Model> {
    /**
     * A StateFlow representing the current synchronization status.
     */
    val syncStatus: StateFlow<SyncStatus>

    /**
     * Queues a synchronization operation.
     *
     * @param operation The synchronization operation to be queued.
     */
    suspend fun queueOperation(operation: SyncOperation<Model>)
}