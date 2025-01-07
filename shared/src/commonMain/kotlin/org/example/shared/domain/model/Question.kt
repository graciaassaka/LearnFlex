package org.example.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * A sealed class representing different types of questions used in quizzes or assessments.
 */
sealed class Question {

    abstract val text: String
    abstract val correctAnswer: Any

    /**
     * Represents a multiple-choice question within a quiz system.
     *
     * @property text The text of the question.
     * @property options A list of available options for the question.
     * @property correctAnswer The value of the correct answer from the options.
     */
    @Serializable
    data class MultipleChoice(
        override val text: String,
        override val correctAnswer: String,
        val options: List<Option>
    ) : Question() {
        /**
         * Represents an option in a multiple-choice question.
         *
         * @property letter The letter associated with the option, typically used for display purposes.
         * @property value The value associated with the option, typically used internally to represent the option.
         */
        @Serializable
        data class Option(
            val letter: String,
            val value: String
        )
    }

    /**
     * Represents a True/False question within a quiz or a questionnaire.
     *
     * @property text The statement or question to be evaluated as True or False.
     * @property correctAnswer The correct answer for the question; true if the statement is correct, false otherwise.
     */
    @Serializable
    data class TrueFalse(
        override val text: String,
        override val correctAnswer: Boolean
    ) : Question()

    /**
     * Represents an ordering question where the user is expected to arrange a set of items
     * into the correct order.
     *
     * @property text The text or prompt of the question.
     * @property correctAnswer The list of items in the correct sequential order for this question.
     */
    @Serializable
    data class Ordering(
        override val text: String,
        override val correctAnswer: List<String>
    ) : Question()
}