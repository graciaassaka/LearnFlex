package org.example.shared.domain.constant

sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object InProgress : SyncStatus()
    data object Success : SyncStatus()
    data class Error(val error: Throwable) : SyncStatus()
}