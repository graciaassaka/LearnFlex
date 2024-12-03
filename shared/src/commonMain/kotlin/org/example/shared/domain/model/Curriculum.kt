package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.StatusQueryable

/**
 * Data class representing a curriculum, which includes a syllabus,
 * status information, and timestamps for creation and last update.
 */
@Serializable
open class Curriculum(
    @SerialName("id")
    override val id: String,

    @SerialName("image_url")
    open val imageUrl: String,

    @SerialName("syllabus")
    open val syllabus: String,

    @SerialName("description")
    open val description: String,

    @SerialName("status")
    override val status: String,

    @SerialName("created_at")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord, StatusQueryable
