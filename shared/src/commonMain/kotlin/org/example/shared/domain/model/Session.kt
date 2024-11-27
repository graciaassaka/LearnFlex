package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.contract.DatabaseRecord

@Serializable
open class Session(
    @SerialName("id")
    override val id: String,

    @SerialName("lesson_id")
    open val lessonId: String,

    @SerialName("start_time_ms")
    open val startTimeMs: Long,

    @SerialName("end_time_ms")
    open val endTimeMs: Long,

    @SerialName("duration_minutes")
    open val durationMinutes: Long,

    @SerialName("score")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord