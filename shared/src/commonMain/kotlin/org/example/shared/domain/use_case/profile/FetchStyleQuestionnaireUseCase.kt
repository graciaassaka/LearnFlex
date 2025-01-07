package org.example.shared.domain.use_case.profile

import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.model.Profile

/**
 * Use case for getting the style questionnaire.
 *
 * @property styleQuizGeneratorClient The service used to generate the quiz.
 */
class FetchStyleQuestionnaireUseCase(private val styleQuizGeneratorClient: StyleQuizGeneratorClient) {

    /**
     * Invokes the use case to generate a style quiz based on learning preferences.
     *
     * @param preferences The learning preferences to generate the quiz.
     * @param number The number of questions to generate.
     * @return The generated quiz.
     */
    operator fun invoke(preferences: Profile.LearningPreferences, number: Int = NUMBER_OF_QUESTIONS) =
        styleQuizGeneratorClient.streamQuestions(preferences, number)

    companion object {
        const val NUMBER_OF_QUESTIONS = 4
    }
}