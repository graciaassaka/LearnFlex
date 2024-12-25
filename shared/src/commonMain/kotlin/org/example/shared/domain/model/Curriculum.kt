package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.StatusQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Data class representing a curriculum, which includes a title,
 * status information, and timestamps for creation and last update.
 */

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Curriculum(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("image_url")
    val imageUrl: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("status")
    override val status: String,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, StatusQueryable
