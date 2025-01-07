package org.example.shared.domain.use_case.module

import org.example.shared.domain.constant.Status

/**
 * Use case for summing the modules by status.
 *
 * @property getByCurriculumId The use case to get all modules.
 */
class CountModulesByStatusUseCase(private val getByCurriculumId: RetrieveModulesByCurriculumUseCase) {

    /**
     * Invokes the use case to sum the modules by status.
     *
     * @param curriculumId The curriculum Id.
     * @return The sum of the modules by status.
     */
    suspend operator fun invoke(curriculumId: String) = runCatching {
        getByCurriculumId(curriculumId)
            .getOrThrow().let { modules ->
                modules.groupBy {
                    if (it.quizScore >= it.quizScoreMax * 0.75) Status.FINISHED
                    else Status.UNFINISHED
                }.mapValues { it.value.size }
            }
    }
}