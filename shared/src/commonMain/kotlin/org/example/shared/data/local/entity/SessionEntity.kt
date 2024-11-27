package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.domain.model.Session

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

    @ColumnInfo(name = "start_time_ms")
    override val startTimeMs: Long,

    @ColumnInfo(name = "end_time_ms")
    override val endTimeMs: Long,

    @ColumnInfo(name = "duration_minutes")
    override val durationMinutes: Long,

    @ColumnInfo(name = "completed")
    override val createdAt: Long,

    @ColumnInfo(name = "last_updated")
    override val lastUpdated: Long
) : Session(id, lessonId, startTimeMs, endTimeMs, durationMinutes, createdAt, lastUpdated)
