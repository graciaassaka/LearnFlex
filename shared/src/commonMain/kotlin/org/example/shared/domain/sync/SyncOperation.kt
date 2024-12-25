package org.example.shared.domain.sync

/**
 * Class representing a sync operation.
 *
 * @param type The type of the operation.
 * @param path The path of the operation.
 * @param data The data of the operation.
 * @param timestamp The timestamp of the operation.
 */
data class SyncOperation<Model>(
    val type: SyncOperationType,
    val path: String,
    val data: List<Model>,
    val timestamp: Long
) {
    /**
     * Enum class representing the type of a sync operation.
     */
    enum class SyncOperationType {
        INSERT,
        UPDATE,
        DELETE,
        SYNC,
        INSERT_ALL,
        UPDATE_ALL,
        DELETE_ALL
    }
}