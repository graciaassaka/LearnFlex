package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.StatusQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Curriculum represents a collection of learning content.
 *
 * @property id The unique identifier of the curriculum.
 * @property title The title of the curriculum.
 * @property description A brief description of the curriculum.
 * @property content The content of the curriculum.
 * @property status The status of the curriculum.
 * @property createdAt The timestamp when the curriculum was created.
 * @property lastUpdated The timestamp when the curriculum was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Curriculum(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("content")
    val content: List<String>,

    @SerialName("status")
    override val status: String = Status.UNFINISHED.name,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, StatusQueryable
