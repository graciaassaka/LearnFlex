package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.domain.model.Lesson

/**
 * Entity class representing a lesson in the local database.
 *
 * @property id The unique identifier of the lesson.
 * @property moduleId The identifier of the module this lesson belongs to.
 * @property imageUrl The URL of the lesson's image.
 * @property title The title of the lesson.
 * @property description A brief description of the lesson.
 * @property index The index position of the lesson within the module.
 * @property quizScore The score of the quiz associated with the lesson.
 * @property createdAt The timestamp when the lesson was created.
 * @property lastUpdated The timestamp when the lesson was last updated.
 */
@Entity(
    tableName = "lesson",
    foreignKeys = [
        ForeignKey(
            entity = ModuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["module_id"],
            onDelete = CASCADE
        )
    ]
)
data class LessonEntity(
    @PrimaryKey
    override val id: String,

    @ColumnInfo(
        name = "module_id",
        index = true
    )
    val moduleId: String,

    @ColumnInfo(name = "image_url")
    override val imageUrl: String,

    @ColumnInfo(name = "title")
    override val title: String,

    @ColumnInfo(name = "description")
    override val description: String,

    @ColumnInfo(name = "index")
    override val index: Int,

    @ColumnInfo(name = "quiz_score")
    override val quizScore: Int,

    @ColumnInfo(name = "created_at")
    override val createdAt: Long,

    @ColumnInfo(name = "last_updated")
    override val lastUpdated: Long
) : Lesson(id, imageUrl, title, description, index, quizScore, createdAt, lastUpdated)