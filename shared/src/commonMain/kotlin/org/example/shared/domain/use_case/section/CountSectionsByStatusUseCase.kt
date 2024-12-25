package org.example.shared.domain.use_case.section

import org.example.shared.domain.constant.Status

/**
 * Use case for summing sections by their status.
 *
 * @property getByCurriculumId The use case to get all sections.
 */
class CountSectionsByStatusUseCase(private val getByCurriculumId: GetSectionsByCurriculumIdUseCase) {

    /**
     * Invokes the use case to sum sections by their status.
     *
     * @param curriculumId The curriculum Id.
     * @return A result containing a map of ContentStatus to the count of sections.
     */
    suspend operator fun invoke(curriculumId: String) = kotlin.runCatching {
        getByCurriculumId(curriculumId)
            .getOrThrow().let { modules ->
                modules.groupBy {
                    if (it.quizScore >= it.quizScoreMax * 0.75) Status.FINISHED
                    else Status.UNFINISHED
                }.mapValues { it.value.size }
            }
    }
}