package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.domain.model.definition.ScoreQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity class representing a section in the local database.
 *
 * @property id The unique identifier of the section.
 * @property lessonId The identifier of the lesson this section belongs to.
 * @property imageUrl The URL of the section's image.
 * @property index The index position of the section within the lesson.
 * @property title The title of the section.
 * @property description A brief description of the section.
 * @property content The content of the section.
 * @property imageUrl The URL of the section's image.
 * @property quizScore The score of the quiz associated with the section.
 * @property createdAt The timestamp when the section was created.
 * @property lastUpdated The timestamp when the section was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Entity(
    tableName = "sections",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lesson_id"],
            onDelete = CASCADE
        )
    ]
)
data class SectionEntity(
    @PrimaryKey
    override val id: String = Uuid.random().toString(),

    @ColumnInfo(
        name = "lesson_id",
        index = true
    )
    val lessonId: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String,

    @ColumnInfo(name = "index")
    val index: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "quiz_score")
    override val quizScore: Int,

    @ColumnInfo(name = "quiz_score_max")
    override val quizScoreMax: Int,

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
) : RoomEntity, ScoreQueryable