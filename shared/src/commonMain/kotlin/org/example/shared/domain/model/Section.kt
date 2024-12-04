package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.ScoreQueryable

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
@Serializable
open class Section(
    @SerialName("id")
    override val id: String,

    @SerialName("image_url")
    open val imageUrl: String,

    @SerialName("index")
    open val index: Int,

    @SerialName("title")
    open val title: String,

    @SerialName("description")
    open val description: String,

    @SerialName("content")
    open val content: String,

    @SerialName("quiz_score")
    override val quizScore: Int,

    @SerialName("created_at")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord, ScoreQueryable