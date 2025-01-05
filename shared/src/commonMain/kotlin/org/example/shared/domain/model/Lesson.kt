package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.ScoreQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Lesson represents a part of a module, including details like title, description, and quiz score.
 *
 * @property id The unique identifier of the lesson.
 * @property title The title of the lesson.
 * @property description A brief description of the lesson.
 * @property index The index position of the lesson within the module.
 * @property content The content of the lesson.
 * @property quizScore The score of the quiz associated with the lesson.
 * @property quizScoreMax The maximum score possible for the quiz.
 * @property createdAt The timestamp when the lesson was created.
 * @property lastUpdated The timestamp when the lesson was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Lesson(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("index")
    val index: Int,

    @SerialName("content")
    val content: List<String>,

    @SerialName("quiz_score")
    override val quizScore: Int = 0,

    @SerialName("quiz_score_max")
    override val quizScoreMax: Int = 10,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, ScoreQueryable
