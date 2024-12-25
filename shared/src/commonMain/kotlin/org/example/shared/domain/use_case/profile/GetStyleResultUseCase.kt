package org.example.shared.domain.use_case.profile

import org.example.shared.domain.client.StyleQuizGenerator
import org.example.shared.domain.constant.Style

/**
 * Use case for getting the style result.
 *
 * @property styleQuizGenerator The service used to evaluate style responses.
 */
class GetStyleResultUseCase(private val styleQuizGenerator: StyleQuizGenerator) {
    /**
     * Invokes the use case to evaluate the given styles.
     *
     * @param styles The list of styles to be evaluated.
     * @return The result of the evaluation.
     */
    operator fun invoke(styles: List<Style>) = styleQuizGenerator.evaluateResponses(styles)
}