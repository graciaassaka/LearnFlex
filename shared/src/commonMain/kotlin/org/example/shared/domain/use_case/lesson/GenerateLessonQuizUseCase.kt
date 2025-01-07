package org.example.shared.domain.use_case.lesson

import org.example.shared.domain.constant.Level
import org.example.shared.domain.use_case.quiz.GenerateQuizUseCase

/**
 * Use case for generating a quiz for a lesson.
 *
 * @property generateQuizUseCase The use case responsible for generating quizzes.
 */
class GenerateLessonQuizUseCase(
    private val generateQuizUseCase: GenerateQuizUseCase
) {
    /**
     * Invokes the use case to generate a quiz for a lesson based on the given topic and level.
     *
     * @param topic The topic of the lesson for which the quiz is generated.
     * @param level The level of difficulty for the quiz.
     * @return A flow emitting the generated quiz.
     */
    operator fun invoke(topic: String, level: Level) =
        generateQuizUseCase(topic, level, NUMBER_OF_QUESTIONS)

    companion object {
        const val NUMBER_OF_QUESTIONS = 4
    }
}