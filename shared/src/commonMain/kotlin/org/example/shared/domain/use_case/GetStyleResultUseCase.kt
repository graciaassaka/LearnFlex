package org.example.shared.domain.use_case

import org.example.shared.domain.constant.Style
import org.example.shared.domain.service.StyleQuizClient

/**
 * Use case for getting the style result.
 *
 * @property styleQuizClient The service used to evaluate style responses.
 */
class GetStyleResultUseCase(private val styleQuizClient: StyleQuizClient) {
    /**
     * Invokes the use case to evaluate the given styles.
     *
     * @param styles The list of styles to be evaluated.
     * @return The result of the evaluation.
     */
    operator fun invoke(styles: List<Style>) = styleQuizClient.evaluateResponses(styles)
}