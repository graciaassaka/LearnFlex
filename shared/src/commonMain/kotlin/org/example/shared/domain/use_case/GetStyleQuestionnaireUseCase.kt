package org.example.shared.domain.use_case

import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.service.StyleQuizService

/**
 * Use case for getting the style questionnaire.
 *
 * @property styleQuizService The service used to generate the quiz.
 */
class GetStyleQuestionnaireUseCase(private val styleQuizService: StyleQuizService) {

    /**
     * Invokes the use case to generate a style quiz based on learning preferences.
     *
     * @param preferences The learning preferences to generate the quiz.
     * @return The generated quiz.
     */
    suspend operator fun invoke(preferences: LearningPreferences) = styleQuizService.generateQuiz(preferences)
}