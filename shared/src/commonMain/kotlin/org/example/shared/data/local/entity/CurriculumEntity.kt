package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.domain.model.definition.StatusQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity class representing a curriculum in the local database.
 *
 * @property id The unique identifier of the curriculum.
 * @property userId The identifier of the user this curriculum belongs to.
 * @property imageUrl The URL of the curriculum's image.
 * @property syllabus The detailed syllabus of the curriculum.
 * @property status The current status of the curriculum.
 * @property createdAt The timestamp when the curriculum was created.
 * @property lastUpdated The timestamp when the curriculum was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Entity(
    tableName = "curricula",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = CASCADE
        )
    ]
)
data class CurriculumEntity(
    @PrimaryKey
    override val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "user_id", index = true)
    val userId: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String,

    @ColumnInfo(name = "syllabus")
    val syllabus: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "status")
    override val status: String,

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
) : RoomEntity, StatusQueryable