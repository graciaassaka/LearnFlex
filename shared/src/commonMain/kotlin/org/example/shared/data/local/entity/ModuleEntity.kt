package org.example.shared.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import org.example.shared.domain.model.Module

/**
 * Entity class representing a module in the local database.
 *
 * @property id The unique identifier of the module.
 * @property curriculumId The identifier of the curriculum this module belongs to.
 * @property imageUrl The URL of the module's image.
 * @property title The title of the module.
 * @property description A brief description of the module.
 * @property index The index position of the module within the curriculum.
 * @property quizScore The score of the quiz associated with the module.
 * @property createdAt The timestamp when the module was created.
 * @property lastUpdated The timestamp when the module was last updated.
 */
@Entity(
    tableName = "module",
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
    override val id: String,

    @ColumnInfo(
        name = "curriculum_id",
        index = true
    )
    val curriculumId: String,

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
) : Module(id, imageUrl, title, description, index, quizScore, createdAt, lastUpdated)
