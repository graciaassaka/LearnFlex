package org.example.shared.domain.sync

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface representing a manager for synchronizing operations.
 *
 * @param Model The type of the model being synchronized.
 */
interface SyncManager<Model> {
    /**
     * Represents the synchronization status.
     */
    sealed class SyncStatus {

        /**
         * Indicates that synchronization is idle.
         */
        data object Idle : SyncStatus()

        /**
         * Indicates that synchronization is in progress.
         */
        data object InProgress : SyncStatus()

        /**
         * Indicates that synchronization was successful.
         */
        data object Success : SyncStatus()

        /**
         * Indicates that an error occurred during synchronization.
         *
         * @property error The error that occurred.
         */
        data class Error(val error: Throwable) : SyncStatus()
    }

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