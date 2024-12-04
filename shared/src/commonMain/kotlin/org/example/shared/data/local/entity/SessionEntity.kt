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
 * @property userId The identifier of the lesson this session belongs to.
 * @property endTime The timestamp when the session ended.
 * @property durationMinutes The duration of the session in minutes.
 * @property createdAt The timestamp when the session was created.
 * @property lastUpdated The timestamp when the session was last updated.
 */
@Entity(
    tableName = "session",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = CASCADE
        )
    ]
)
data class SessionEntity(
    @PrimaryKey
    override val id: String,

    @ColumnInfo(name = "user_id", index = true)
    override val userId: String,

    @ColumnInfo(name = "end_time_ms")
    override val endTime: Long,

    @ColumnInfo(name = "duration_minutes")
    override val durationMinutes: Long,

    @ColumnInfo(name = "created_at")
    override val createdAt: Long,

    @ColumnInfo(name = "last_updated")
    override val lastUpdated: Long
) : Session(id, userId, endTime, durationMinutes, createdAt, lastUpdated), RoomEntity
