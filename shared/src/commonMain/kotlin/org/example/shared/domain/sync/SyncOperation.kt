package org.example.shared.domain.sync

import org.example.shared.domain.constant.SyncOperationType

interface SyncOperation<T> {
    val type: SyncOperationType
    val data: T
}