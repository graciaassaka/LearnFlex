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
 * Entity class representing a module in the local database.
 *
 * @property id The unique identifier of the module.
 * @property curriculumId The identifier of the curriculum this module belongs to.
 * @property index The index position of the module within the curriculum.
 * @property title The title of the module.
 * @property description A brief description of the module.
 * @property content The content of the module.
 * @property quizScore The score of the quiz associated with the module.
 * @property quizScoreMax The maximum score possible for the quiz.
 * @property createdAt The timestamp when the module was created.
 * @property lastUpdated The timestamp when the module was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Entity(
    tableName = "modules",
    foreignKeys = [
        ForeignKey(
            entity = CurriculumEntity::class,
            parentColumns = ["id"],
            childColumns = ["curriculum_id"],
            onDelete = CASCADE
        )
    ]
)
data class ModuleEntity(
    @PrimaryKey
    override val id: String = Uuid.random().toString(),

    @ColumnInfo(
        name = "curriculum_id",
        index = true
    )
    val curriculumId: String,

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