package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile

/**
 * Service interface for generating and evaluating style quizzes.
 */
interface StyleQuizGenerator {

    /**
     * Represents a style question with a list of options and a scenario.
     *
     * @property options The list of style options.
     * @property scenario The scenario description.
     */
    @Serializable
    data class StyleQuestion(
        val options: List<StyleOption>,
        val scenario: String
    )

    /**
     * Represents a style option with a style and text.
     *
     * @property style The style identifier.
     * @property text The text description of the style.
     */
    @Serializable
    data class StyleOption(
        val style: String,
        val text: String
    )

    /**
     * Generates a style questionnaire based on the user's learning preferences.
     *
     * @param preferences The user's learning preferences.
     * @return A result containing the style questionnaire.
     */
    fun streamQuestions(preferences: Profile.LearningPreferences, number: Int): Flow<Result<StyleQuestion>>
    /**
     * Evaluates a list of style responses to determine the dominant learning style and provide a style breakdown.
     *
     * @param responses A list of responses representing the user's preferred learning styles.
     * @return A result containing the dominant style and the style breakdown.
     */
    fun evaluateResponses(responses: List<Style>): Result<Profile.LearningStyle>
}