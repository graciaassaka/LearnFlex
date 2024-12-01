package org.example.shared.domain.sync

import org.example.shared.domain.constant.SyncOperationType

data class SyncOperation<Model>(
    val type: SyncOperationType,
    val path: String,
    val data: Model
)