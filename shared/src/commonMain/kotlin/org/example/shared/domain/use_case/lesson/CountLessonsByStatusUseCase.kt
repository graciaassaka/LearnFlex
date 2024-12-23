package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.constant.ContentStatus

/**
 * Use case for summing the lessons by status.
 *
 * @property getByCurriculumId The use case to get all lessons.
 */
class CountLessonsByStatusUseCase(private val getByCurriculumId: GetLessonsByCurriculumIdUseCase) {

    /**
     * Invokes the use case to sum the lessons by status.
     *
     * @param curriculumId The curriculum Id.
     * @return The sum of the lessons by status.
     */
    suspend operator fun invoke(curriculumId: String) = runCatching {
        getByCurriculumId(curriculumId)
            .getOrThrow().let { modules ->
                modules.groupBy {
                    if (it.quizScore >= it.quizScoreMax * 0.75) ContentStatus.FINISHED
                    else ContentStatus.UNFINISHED
                }.mapValues { it.value.size }
            }
    }
}