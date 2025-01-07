package org.example.shared.domain.sync

import org.example.shared.domain.storage_operations.util.Path

/**
 * Class representing a sync operation.
 *
 * @param type The type of the operation.
 * @param path The path of the operation.
 * @param data The data of the operation.
 * @param timestamp The timestamp of the operation.
 */
data class SyncOperation<Model>(
    val type: Type,
    val path: Path,
    val data: List<Model>,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Enum class representing the type of a sync operation.
     */
    enum class Type {
        INSERT,
        UPDATE,
        DELETE,
        SYNC,
        INSERT_ALL,
        UPDATE_ALL,
        DELETE_ALL
    }
}