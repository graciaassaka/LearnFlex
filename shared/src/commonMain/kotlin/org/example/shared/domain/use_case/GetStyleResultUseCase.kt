package org.example.shared.domain.use_case

import org.example.shared.data.util.Style
import org.example.shared.domain.service.StyleQuizService

/**
 * Use case for getting the style result.
 *
 * @property styleQuizService The service used to evaluate style responses.
 */
class GetStyleResultUseCase (private val styleQuizService: StyleQuizService) {
    /**
     * Invokes the use case to evaluate the given styles.
     *
     * @param styles The list of styles to be evaluated.
     * @return The result of the evaluation.
     */
    operator fun invoke(styles: List<Style>) = styleQuizService.evaluateResponses(styles)
}