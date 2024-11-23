package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.contract.DatabaseRecord

/**
 * Data class representing a curriculum, which includes a syllabus,
 * status information, and timestamps for creation and last update.
 */
@Serializable
data class Curriculum(
    @SerialName("id")
    override val id: String,

    @SerialName("syllabus")
    val syllabus: String,

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord
