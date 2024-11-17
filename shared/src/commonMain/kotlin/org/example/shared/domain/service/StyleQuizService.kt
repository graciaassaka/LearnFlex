package org.example.shared.domain.service

import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.model.StyleQuestionnaire
import org.example.shared.domain.model.StyleResult
import org.example.shared.domain.constant.Style

/**
 * Service interface for generating and evaluating style quizzes.
 */
interface StyleQuizService {
    /**
     * Generates a quiz based on the provided learning preferences.
     *
     * @param preferences The learning preferences which include the field of study, level of expertise, and learning goal.
     * @return A Result wrapping a StyleQuestionnaire that contains a list of style questions tailored to the given preferences.
     */
    suspend fun generateQuiz(preferences: LearningPreferences): Result<StyleQuestionnaire>
    /**
     * Evaluates a list of style responses to determine the dominant learning style and provide a style breakdown.
     *
     * @param responses A list of responses representing the user's preferred learning styles.
     * @return A result containing the dominant style and the style breakdown.
     */
    fun evaluateResponses(responses: List<Style>): Result<StyleResult>
}