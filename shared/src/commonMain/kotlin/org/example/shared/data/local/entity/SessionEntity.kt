package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.domain.model.definition.EndTimeQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity class representing a session in the local database.
 *
 * @property id The unique identifier of the session.
 * @property userId The identifier of the lesson this session belongs to.
 * @property endTime The timestamp when the session ended.
 * @property createdAt The timestamp when the session was created.
 * @property lastUpdated The timestamp when the session was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = CASCADE
        )
    ]
)
data class SessionEntity(
    @PrimaryKey
    override val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "user_id", index = true)
    val userId: String,

    @ColumnInfo(name = "end_time")
    override val endTime: Long,

    @ColumnInfo(
        name = "created_at",
        defaultValue = "CURRENT_TIMESTAMP"
    )
    override val createdAt: Long,

    @ColumnInfo(
        name = "last_updated",
        defaultValue = "CURRENT_TIMESTAMP",
    )
    override val lastUpdated: Long
) : RoomEntity, EndTimeQueryable
