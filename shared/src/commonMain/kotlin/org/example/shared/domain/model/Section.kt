package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.ScoreQueryable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Section represents a part of a lesson with various attributes like title, description, content, etc.
 *
 * @property id The unique identifier of the section.
 * @property index The index position of the section within the lesson.
 * @property title The title of the section.
 * @property description A brief description of the section.
 * @property content The main content of the section.
 * @property imageUrl URL to an image associated with the section.
 * @property quizScore The score of the quiz associated with the section.
 * @property createdAt The timestamp when the section was created.
 * @property lastUpdated The timestamp when the section was last updated.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Section(
    @SerialName("id")
    override val id: String = Uuid.random().toString(),

    @SerialName("image_url")
    val imageUrl: String,

    @SerialName("index")
    val index: Int,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("content")
    val content: String,

    @SerialName("quiz_score")
    override val quizScore: Int,

    @SerialName("quiz_score_max")
    override val quizScoreMax: Int,

    @SerialName("created_at")
    override val createdAt: Long = System.currentTimeMillis(),

    @SerialName("last_updated")
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord, ScoreQueryable