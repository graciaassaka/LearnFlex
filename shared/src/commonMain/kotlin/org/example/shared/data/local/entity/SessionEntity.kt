package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.domain.model.Session

/**
 * Entity class representing a session in the local database.
 *
 * @property id The unique identifier of the session.
 * @property lessonId The identifier of the lesson this session belongs to.
 * @property endTimeMs The timestamp when the session ended.
 * @property durationMinutes The duration of the session in minutes.
 * @property createdAt The timestamp when the session was created.
 * @property lastUpdated The timestamp when the session was last updated.
 */
@Entity(
    tableName = "session",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lesson_id"],
            onDelete = CASCADE
        )
    ]
)
data class SessionEntity(
    @PrimaryKey
    override val id: String,

    @ColumnInfo(name = "lesson_id", index = true)
    override val lessonId: String,

    @ColumnInfo(name = "end_time_ms")
    override val endTimeMs: Long,

    @ColumnInfo(name = "duration_minutes")
    override val durationMinutes: Long,

    @ColumnInfo(name = "created_at")
    override val createdAt: Long,

    @ColumnInfo(name = "last_updated")
    override val lastUpdated: Long
) : Session(id, lessonId, endTimeMs, durationMinutes, createdAt, lastUpdated), RoomEntity
