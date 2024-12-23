package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.StatusQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Data class representing a curriculum, which includes a syllabus,
 * status information, and timestamps for creation and last update.
 */

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Curriculum(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("image_url")
    val imageUrl: String,

    @SerialName("syllabus")
    val syllabus: String,

    @SerialName("description")
    val description: String,

    @SerialName("status")
    override val status: String,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, StatusQueryable
