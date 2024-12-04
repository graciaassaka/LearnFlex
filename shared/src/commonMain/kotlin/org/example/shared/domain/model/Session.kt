package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.EndTimeQueryable

/**
 * Data class representing a session.
 *
 * @property id The unique identifier of the session.
 * @property userId The ID of the user that the session belongs to.
 * @property endTime The timestamp when the session ended.
 * @property durationMinutes The duration of the session in minutes.
 * @property createdAt The timestamp when the session was created.
 * @property lastUpdated The timestamp when the session was last updated.
 */
@Serializable
open class Session(
    @SerialName("id")
    override val id: String,

    @SerialName("lesson_id")
    open val userId: String,

    @SerialName("end_time_ms")
    override val endTime: Long,

    @SerialName("duration_minutes")
    open val durationMinutes: Long,

    @SerialName("created_at")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord, EndTimeQueryable