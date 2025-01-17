package org.example.shared.domain.model.interfaces

/**
 * Represents a record in the database.
 */
interface DatabaseRecord {
    /**
     * Unique identifier for the record.
     */
    val id: String

    /**
     * Timestamp of when the record was created.
     */
    val createdAt: Long

    /**
     * Timestamp of the last update to the record.
     */
    val lastUpdated: Long
}