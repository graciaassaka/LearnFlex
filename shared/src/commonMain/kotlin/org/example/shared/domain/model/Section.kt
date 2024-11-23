package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import org.example.shared.domain.model.contract.DatabaseRecord

/**
 * Section represents a part of a lesson with various attributes like title, description, content, etc.
 *
 * @property id The unique identifier of the section.
 * @property lessonId The identifier of the lesson this section belongs to.
 * @property index The index position of the section within the lesson.
 * @property title The title of the section.
 * @property description A brief description of the section.
 * @property content The main content of the section.
 * @property imageUrl URL to an image associated with the section.
 * @property quizScore The score of the quiz associated with the section.
 * @property createdAt The timestamp when the section was created.
 * @property lastUpdated The timestamp when the section was last updated.
 */
data class Section(
    @SerialName("id")
    override val id: String,

    @SerialName("lesson_id")
    val lessonId: String,

    @SerialName("index")
    val index: Int,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("content")
    val content: String,

    @SerialName("image_url")
    val imageUrl: String,

    @SerialName("quiz_score")
    val quizScore: Int,

    @SerialName("created_at")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord