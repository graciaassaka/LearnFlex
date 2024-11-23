package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import org.example.shared.domain.model.contract.DatabaseRecord

/**
 * Lesson represents a single unit of learning content.
 *
 * @property id The unique identifier of the lesson.
 * @property moduleId The identifier of the module this lesson belongs to.
 * @property title The title of the lesson.
 * @property description A brief description of the lesson.
 * @property index The index position of the lesson within the module.
 * @property quizScore The score of the quiz associated with the lesson.
 * @property createdAt The timestamp when the lesson was created.
 * @property lastUpdated The timestamp when the lesson was last updated.
 */
data class Lesson(
    @SerialName("id")
    override val id: String,

    @SerialName("module_id")
    val moduleId: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("index")
    val index: Int,

    @SerialName("quiz_score")
    val quizScore: Int,

    @SerialName("created_at")
    override val createdAt: Long,

    @SerialName("last_updated")
    override val lastUpdated: Long
) : DatabaseRecord
