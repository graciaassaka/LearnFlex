package org.example.shared.domain.use_case.profile

import org.example.shared.domain.client.StyleQuizGenerator
import org.example.shared.domain.model.Profile

/**
 * Use case for getting the style questionnaire.
 *
 * @property styleQuizGenerator The service used to generate the quiz.
 */
class GetStyleQuestionnaireUseCase(private val styleQuizGenerator: StyleQuizGenerator) {

    /**
     * Invokes the use case to generate a style quiz based on learning preferences.
     *
     * @param preferences The learning preferences to generate the quiz.
     * @return The generated quiz.
     */
    operator fun invoke(preferences: Profile.LearningPreferences, number: Int) =
        styleQuizGenerator.streamQuestions(preferences, number)
}