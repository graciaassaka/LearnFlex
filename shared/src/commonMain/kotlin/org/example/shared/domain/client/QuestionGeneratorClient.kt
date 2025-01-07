package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Question

/**
 * Interface for a client that generates questions.
 *
 * @param T The type of question to be generated, which must extend the [Question] class.
 */
interface QuestionGeneratorClient<T : Question> {

    /**
     * Data class representing the context for generating questions.
     *
     * @property topic The topic of the questions to be generated.
     * @property level The difficulty level of the questions to be generated.
     */
    @Serializable
    data class Context(
        val topic: String,
        val level: Level
    )

    /**
     * Generates a flow of questions based on the provided context and number of questions.
     *
     * @param context The context for generating questions.
     * @param number The number of questions to generate.
     * @return A flow emitting the results of the generated questions.
     */
    fun generateQuestion(context: Context, number: Int): Flow<Result<T>>
}