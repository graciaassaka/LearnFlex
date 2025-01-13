package org.example.shared.domain.use_case.quiz

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import org.example.shared.domain.client.QuestionGeneratorClient
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Question
import kotlin.random.Random

/**
 * Use case for generating a quiz with multiple types of questions.
 *
 * @property multipleChoiceGeneratorClient Client for generating multiple choice questions.
 * @property trueFalseGeneratorClient Client for generating true/false questions.
 * @property orderingGeneratorClient Client for generating ordering questions.
 * @property random Random instance for distributing questions.
 */
class GenerateQuizUseCase(
    private val multipleChoiceGeneratorClient: QuestionGeneratorClient<Question.MultipleChoice>,
    private val trueFalseGeneratorClient: QuestionGeneratorClient<Question.TrueFalse>,
    private val random: Random
) {
    /**
     * Generates a quiz with the specified topic, level, and number of questions.
     *
     * @param topic The topic of the quiz.
     * @param level The difficulty level of the quiz.
     * @param number The total number of questions in the quiz.
     * @return A Flow emitting the generated questions wrapped in a Result.
     */
    operator fun invoke(topic: String, level: Level, number: Int): Flow<Result<Question>> {
        val (multipleChoiceCount, trueFalseCount) = distributeQuestions(number)
        return merge(
            multipleChoiceGeneratorClient.generateQuestion(
                QuestionGeneratorClient.Context(topic, level),
                multipleChoiceCount
            ),
            trueFalseGeneratorClient.generateQuestion(
                QuestionGeneratorClient.Context(topic, level),
                trueFalseCount
            )
        )
    }

    /**
     * Distributes the total number of questions into multiple choice, true/false, and ordering questions.
     *
     * @param total The total number of questions to distribute.
     * @return A Triple containing the counts of multiple choice, true/false, and ordering questions.
     */
    private fun distributeQuestions(total: Int): Pair<Int, Int> {
        val multipleChoiceCount = random.nextInt(0, total)
        val trueFalseCount = total - multipleChoiceCount

        return Pair(multipleChoiceCount, trueFalseCount)
    }
}