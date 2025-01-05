package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.domain.model.interfaces.ScoreQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity class representing a lesson in the local database.
 *
 * @property id The unique identifier of the lesson.
 * @property moduleId The identifier of the module this lesson belongs to.
 * @property index The index position of the lesson within the module.
 * @property title The title of the lesson.
 * @property description A brief description of the lesson.
 * @property content The content of the lesson.
 * @property quizScore The score of the quiz associated with the lesson.
 * @property quizScoreMax The maximum score possible for the quiz.
 * @property createdAt The timestamp when the lesson was created.
 * @property lastUpdated The timestamp when the lesson was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Entity(
    tableName = "lessons",
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
    override val id: String = Uuid.random().toString(),

    @ColumnInfo(
        name = "module_id",
        index = true
    )
    val moduleId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "index")
    val index: Int,

    @ColumnInfo(name = "content")
    val content: List<String>,

    @ColumnInfo(
        name = "quiz_score",
        defaultValue = "0"
    )
    override val quizScore: Int,

    @ColumnInfo(
        name = "quiz_score_max",
        defaultValue = "10"
    )
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