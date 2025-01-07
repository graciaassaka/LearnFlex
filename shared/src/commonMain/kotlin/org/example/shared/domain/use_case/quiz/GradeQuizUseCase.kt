package org.example.shared.domain.use_case.quiz

import org.example.shared.domain.model.Question

/**
 * Use case for grading a quiz.
 */
class GradeQuizUseCase {

    /**
     * Grades the quiz based on the provided questions and answers.
     *
     * @param questions The list of questions in the quiz.
     * @param answers The list of answers provided by the user.
     * @param maxScore The maximum score that can be achieved in the quiz.
     * @return The calculated score wrapped in a Result.
     * @throws IllegalArgumentException if the size of questions and answers do not match, or if maxScore is not greater than zero.
     */
    operator fun invoke(questions: List<Question>, answers: List<Any>, maxScore: Int) = runCatching {
        require(questions.size == answers.size) { "Questions and answers size mismatch" }
        require(maxScore > 0) { "Max score must be greater than zero" }

        questions.map { it.correctAnswer }
            .zip(answers)
            .count { (correctAnswer, answer) -> correctAnswer == answer }
            .run { (toDouble() / questions.size * maxScore).toInt() }
    }
}