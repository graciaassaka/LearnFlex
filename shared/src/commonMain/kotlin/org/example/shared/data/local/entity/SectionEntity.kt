package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.domain.model.Section

/**
 * Entity class representing a section in the local database.
 *
 * @property id The unique identifier of the section.
 * @property lessonId The identifier of the lesson this section belongs to.
 * @property index The index position of the section within the lesson.
 * @property title The title of the section.
 * @property description A brief description of the section.
 * @property content The content of the section.
 * @property imageUrl The URL of the section's image.
 * @property quizScore The score of the quiz associated with the section.
 * @property createdAt The timestamp when the section was created.
 * @property lastUpdated The timestamp when the section was last updated.
 */
@Entity(
    tableName = "section",
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
    override val id: String,

    @ColumnInfo(name = "lesson_id")
    override val lessonId: String,

    @ColumnInfo(name = "index")
    override val index: Int,

    @ColumnInfo(name = "title")
    override val title: String,

    @ColumnInfo(name = "description")
    override val description: String,

    @ColumnInfo(name = "content")
    override val content: String,

    @ColumnInfo(name = "image_url")
    override val imageUrl: String,

    @ColumnInfo(name = "quiz_score")
    override val quizScore: Int,

    @ColumnInfo(name = "created_at")
    override val createdAt: Long,

    @ColumnInfo(name = "last_updated")
    override val lastUpdated: Long
) : Section(id, lessonId, index, title, description, content, imageUrl, quizScore, createdAt, lastUpdated)
