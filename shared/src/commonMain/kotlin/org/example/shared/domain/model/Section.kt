package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.ScoreQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Module represents a collection of lessons, including details like title, description, and quiz score.
 *
 * @property id The unique identifier of the module.
 * @property title The title of the module.
 * @property description A brief description of the module.
 * @property index The index position of the module within the curriculum.
 * @property content The content of the module.
 * @property quizScore The score of the quiz associated with the module.
 * @property quizScoreMax The maximum score possible for the quiz.
 * @property createdAt The timestamp when the module was created.
 * @property lastUpdated The timestamp when the module was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Section(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("index")
    val index: Int,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("content")
    val content: String,

    @SerialName("quiz_score")
    override val quizScore: Int = 0,

    @SerialName("quiz_score_max")
    override val quizScoreMax: Int = 10,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, ScoreQueryable