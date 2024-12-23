package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.EndTimeQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Data class representing a session.
 *
 * @property id The unique identifier of the session.
 * @property endTime The timestamp when the session ended.
 * @property createdAt The timestamp when the session was created.
 * @property lastUpdated The timestamp when the session was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Session(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("end_time")
    override val endTime: Long,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, EndTimeQueryable