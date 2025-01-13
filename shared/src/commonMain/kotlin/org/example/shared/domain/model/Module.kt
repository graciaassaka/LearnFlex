package org.example.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.DescribableRecord
import org.example.shared.domain.model.interfaces.ScorableRecord
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Module represents a collection of lessons, including details like title, description, and quiz score.
 *
 * @property id The unique identifier of the module.
 * @property title The title of the module.
 * @property description A brief description of the module.
 * @property content The content of the module.
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

    @SerialName("title")
    override val title: String,

    @SerialName("description")
    override val description: String,

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
) : DatabaseRecord, ScorableRecord, DescribableRecord {
    /**
     * Determines if the quiz can be taken based on the completion status of the lessons.
     *
     * @param lessons The list of lessons to check.
     * @return `true` if all lessons are completed and the list is not empty, `false` otherwise.
     */
    fun canQuiz(lessons: List<Lesson>) = lessons.isNotEmpty() && lessons.all { it.isCompleted() }

    /**
     * Updates the quiz score if the new score is higher than the current score.
     *
     * @param score The new score to update the quiz score with.
     * @return A new instance of the module with the updated quiz score.
     */
    fun updateQuizScore(score: Int) = copy(quizScore = maxOf(score, quizScore))
}
