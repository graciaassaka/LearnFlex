package org.example.shared.data.sync.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.sync.SyncOperation

/**
 * Implementation of the SyncManager interface.
 *
 * @param Model The type of data to be synchronized.
 * @param syncScope The CoroutineScope in which synchronization operations will be executed.
 * @param syncHandler The handler responsible for processing synchronization operations.
 * @param maxRetries The maximum number of retries for a failed synchronization operation.
 */
class SyncManagerImpl<Model>(
    syncScope: CoroutineScope,
    private val syncHandler: SyncHandler<Model>,
    private val maxRetries: Int = 3
) : SyncManager<Model>, AutoCloseable {
    private val pendingOperations = Channel<SyncOperation<Model>>(Channel.UNLIMITED)
    private val _syncStatus = MutableStateFlow<SyncManager.SyncStatus>(SyncManager.SyncStatus.Idle)
    override val syncStatus = _syncStatus.asStateFlow()
    private val job = syncScope.launch { for (operation in pendingOperations) processOperation(operation) }


    /**
     * Queues a synchronization operation to be processed.
     *
     * @param operation The synchronization operation to be queued.
     */
    override suspend fun queueOperation(operation: SyncOperation<Model>) = pendingOperations.send(operation)

    /**
     * Processes a synchronization operation.
     *
     * @param operation The synchronization operation to be processed.
     * @param retryCount The current retry count for the operation.
     */
    private suspend fun processOperation(
        operation: SyncOperation<Model>,
        retryCount: Int = 0
    ): Unit = try {
        _syncStatus.value = SyncManager.SyncStatus.InProgress
        syncHandler.handleSync(operation)
        _syncStatus.value = SyncManager.SyncStatus.Success
    } catch (e: Throwable) {
        if (retryCount < maxRetries) {
            delay(1000L * (1 shl retryCount))
            processOperation(operation, retryCount + 1)
        } else {
            _syncStatus.value = SyncManager.SyncStatus.Error(e)
        }
    }

    /**
     * Closes the SyncManager, cancelling any ongoing operations and closing the pending operations channel.
     */
    override fun close() {
        job.cancel()
        pendingOperations.close()
    }
}
