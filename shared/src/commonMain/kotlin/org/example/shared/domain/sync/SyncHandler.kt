package org.example.shared.domain.sync

/**
 * Interface representing a handler for synchronization operations.
 *
 * @param T the type of the data being synchronized
 */
interface SyncHandler<T> {
    /**
     * Handles a synchronization operation.
     *
     * @param operation the synchronization operation to handle
     */
    suspend fun handleSync(operation: SyncOperation<T>)
}