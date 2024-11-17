package org.example.shared.domain.sync

interface SyncHandler<T> {
    suspend fun handleSync(operation: SyncOperation<T>)
}