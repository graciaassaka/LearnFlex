package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.model.StyleQuestion

/**
 * Service interface for generating and evaluating style quizzes.
 */
interface StyleQuizClient {
    /**
     * Generates a style questionnaire based on the user's learning preferences.
     *
     * @param preferences The user's learning preferences.
     * @return A result containing the style questionnaire.
     */
    fun streamQuestions(preferences: LearningPreferences, number: Int): Flow<Result<StyleQuestion>>
    /**
     * Evaluates a list of style responses to determine the dominant learning style and provide a style breakdown.
     *
     * @param responses A list of responses representing the user's preferred learning styles.
     * @return A result containing the dominant style and the style breakdown.
     */
    fun evaluateResponses(responses: List<Style>): Result<LearningStyle>
}