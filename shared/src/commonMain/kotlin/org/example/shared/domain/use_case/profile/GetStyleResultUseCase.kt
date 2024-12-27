package org.example.shared.domain.use_case.profile

import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.constant.Style

/**
 * Use case for getting the style result.
 *
 * @property styleQuizGeneratorClient The service used to evaluate style responses.
 */
class GetStyleResultUseCase(private val styleQuizGeneratorClient: StyleQuizGeneratorClient) {
    /**
     * Invokes the use case to evaluate the given styles.
     *
     * @param styles The list of styles to be evaluated.
     * @return The result of the evaluation.
     */
    operator fun invoke(styles: List<Style>) = styleQuizGeneratorClient.evaluateResponses(styles)
}