package org.example.shared.domain.use_case.profile

import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.model.Profile

/**
 * Use case for getting the style questionnaire.
 *
 * @property styleQuizGeneratorClient The service used to generate the quiz.
 */
class GetStyleQuestionnaireUseCase(private val styleQuizGeneratorClient: StyleQuizGeneratorClient) {

    /**
     * Invokes the use case to generate a style quiz based on learning preferences.
     *
     * @param preferences The learning preferences to generate the quiz.
     * @return The generated quiz.
     */
    operator fun invoke(preferences: Profile.LearningPreferences, number: Int) =
        styleQuizGeneratorClient.streamQuestions(preferences, number)
}