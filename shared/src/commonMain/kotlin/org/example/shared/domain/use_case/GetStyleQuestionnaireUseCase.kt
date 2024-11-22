package org.example.shared.domain.use_case

import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.service.StyleQuizClient

/**
 * Use case for getting the style questionnaire.
 *
 * @property styleQuizClient The service used to generate the quiz.
 */
class GetStyleQuestionnaireUseCase(private val styleQuizClient: StyleQuizClient) {

    /**
     * Invokes the use case to generate a style quiz based on learning preferences.
     *
     * @param preferences The learning preferences to generate the quiz.
     * @return The generated quiz.
     */
    operator fun invoke(preferences: LearningPreferences, number: Int) =
        styleQuizClient.streamQuestions(preferences, number)
}