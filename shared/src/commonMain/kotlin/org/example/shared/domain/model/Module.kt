package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.ScoreQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Module represents a part of a curriculum, including details like title, description, and quiz score.
 *
 * @property id The unique identifier of the module.
 * @property title The title of the module.
 * @property description A brief description of the module.
 * @property index The index position of the module within the curriculum.
 * @property quizScore The score of the quiz associated with the module.
 * @property quizScoreMax The maximum score possible for the quiz.
 * @property createdAt The timestamp when the module was created.
 * @property lastUpdated The timestamp when the module was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Module(
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
