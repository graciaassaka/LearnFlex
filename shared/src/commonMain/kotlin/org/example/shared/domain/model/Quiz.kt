package org.example.shared.domain.model


/**
 * Represents a quiz with a set of questions, their answers, and scoring details.
 *
 * @property owner The owner or creator of the quiz.
 * @property questionNumber The current question number in the quiz.
 * @property questions A list of questions in the quiz.
 * @property answers A list of answers provided for the quiz questions.
 * @property maxScore The maximum score achievable in the quiz.
 * @property score The current score obtained in the quiz.
 *
 * @constructor Ensures that the maximum score is non-negative when creating a quiz.
 */
data class Quiz(
    val owner: String = "",
    val questionNumber: Int = 0,
    val questions: List<Question> = emptyList(),
    val answers: List<Any> = emptyList(),
    val maxScore: Int = 10,
    val score: Int = 0
) {
    init {
        require(maxScore >= 0)
    }

    /**
     * Calculates the score of the quiz based on the correct answers.
     *
     * @return The calculated score as an integer.
     */
    fun grade(): Quiz {
        require(questions.size == answers.size)

        return this.copy(score = questions.map { it.correctAnswer }
            .zip(answers)
            .count { (correctAnswer, answer) -> correctAnswer == answer }
            .run { (toDouble() / questions.size * maxScore).toInt() }
        )
    }
}
