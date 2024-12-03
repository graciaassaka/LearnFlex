package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.ScoreQueryable

/**
 * Lesson represents a single unit of learning content.
 *
 * @property id The unique identifier of the lesson.
 * @property imageUrl URL to an image associated with the lesson.
 * @property title The title of the lesson.
 * @property description A brief description of the lesson.
 * @property index The index position of the lesson within the module.
 * @property quizScore The score of the quiz associated with the lesson.
 * @property createdAt The timestamp when the lesson was created.
 * @property lastUpdated The timestamp when the lesson was last updated.
 */
open class Lesson(
    @SerialName("id")
    override val id: String,

    @SerialName("image_url")
    open val imageUrl: String,

    @SerialName("title")
    open val title: String,

    @SerialName("description")
    open val description: String,

    @SerialName("index")
    open val index: Int,

    @SerialName("quiz_score")
    override val quizScore: Int,

    @SerialName("created_at")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord, ScoreQueryable
