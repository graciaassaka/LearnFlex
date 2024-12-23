package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.ScoreQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Lesson represents a single unit of learning content.
 *
 * @property id The unique identifier of the lesson.
 * @property imageUrl URL to an image associated with the lesson.
 * @property title The title of the lesson.
 * @property description A brief description of the lesson.
 * @property index The index position of the lesson within the module.
 * @property quizScore The score of the quiz associated with the lesson.
 * @property quizScoreMax The maximum score of the quiz associated with the lesson.
 * @property createdAt The timestamp when the lesson was created.
 * @property lastUpdated The timestamp when the lesson was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Lesson(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("image_url")
    val imageUrl: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("index")
    val index: Int,

    @SerialName("quiz_score")
    override val quizScore: Int,

    @SerialName("quiz_score_max")
    override val quizScoreMax: Int,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, ScoreQueryable
