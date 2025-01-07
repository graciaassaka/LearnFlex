package org.example.shared.domain.use_case.quiz

import org.example.shared.domain.model.Question
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradeQuizUseCaseTest {

    private val gradeQuizUseCase = GradeQuizUseCase()

    // Helper functions to create different types of questions
    private fun createMultipleChoiceQuestion(
        text: String,
        correctAnswer: String,
        options: List<Question.MultipleChoice.Option> = listOf(
            Question.MultipleChoice.Option("A", "Option A"),
            Question.MultipleChoice.Option("B", "Option B")
        )
    ): Question.MultipleChoice {
        return Question.MultipleChoice(
            text = text,
            correctAnswer = correctAnswer,
            options = options
        )
    }

    private fun createTrueFalseQuestion(
        text: String,
        correctAnswer: Boolean
    ): Question.TrueFalse {
        return Question.TrueFalse(
            text = text,
            correctAnswer = correctAnswer
        )
    }

    private fun createOrderingQuestion(
        text: String,
        correctAnswer: List<String>
    ): Question.Ordering {
        return Question.Ordering(
            text = text,
            correctAnswer = correctAnswer
        )
    }

    @Test
    fun `invoke should return maxScore when all answers are correct`() {
        // Arrange
        val questions = listOf(
            createMultipleChoiceQuestion("MCQ 1", "Option A"),
            createTrueFalseQuestion("TFQ 1", true),
            createOrderingQuestion("OQ 1", listOf("first", "second"))
        )
        val answers = listOf(
            "Option A",
            true,
            listOf("first", "second")
        )
        val maxScore = 100

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(maxScore, result.getOrNull())
    }

    @Test
    fun `invoke should return partial score when some answers are correct`() {
        // Arrange
        val questions = listOf(
            createMultipleChoiceQuestion("MCQ 1", "Option A"),
            createTrueFalseQuestion("TFQ 1", true),
            createOrderingQuestion("OQ 1", listOf("first", "second")),
            createMultipleChoiceQuestion("MCQ 2", "Option B")
        )
        val answers = listOf(
            "Option A",        // Correct
            false,             // Incorrect
            listOf("first", "second"), // Correct
            "Option A"         // Incorrect
        )
        val maxScore = 80

        // Correct answers: 2 out of 4
        val expectedScore = (2.0 / 4 * 80).toInt() // 40

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedScore, result.getOrNull())
    }

    @Test
    fun `invoke should return zero when no answers are correct`() {
        // Arrange
        val questions = listOf(
            createMultipleChoiceQuestion("MCQ 1", "Option A"),
            createTrueFalseQuestion("TFQ 1", true)
        )
        val answers = listOf(
            "Option B", // Incorrect
            false        // Incorrect
        )
        val maxScore = 50

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `invoke should fail when questions and answers sizes mismatch`() {
        // Arrange
        val questions = listOf(
            createMultipleChoiceQuestion("MCQ 1", "Option A"),
            createTrueFalseQuestion("TFQ 1", true)
        )
        val answers = listOf(
            "Option A" // Only one answer for two questions
        )
        val maxScore = 100

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Questions and answers size mismatch", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should fail when maxScore is zero`() {
        // Arrange
        val questions = listOf(
            createTrueFalseQuestion("TFQ 1", true)
        )
        val answers = listOf(
            true
        )
        val maxScore = 0

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Max score must be greater than zero", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should fail when maxScore is negative`() {
        // Arrange
        val questions = listOf(
            createOrderingQuestion("OQ 1", listOf("alpha", "beta"))
        )
        val answers = listOf(
            listOf("alpha", "beta")
        )
        val maxScore = -10

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Max score must be greater than zero", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should handle different question types correctly`() {
        // Arrange
        val questions = listOf(
            createMultipleChoiceQuestion("MCQ 1", "Option A"),
            createTrueFalseQuestion("TFQ 1", false),
            createOrderingQuestion("OQ 1", listOf("item1", "item2", "item3")),
            createMultipleChoiceQuestion("MCQ 2", "Option C")
        )
        val answers = listOf(
            "Option A",                        // Correct
            true,                              // Incorrect
            listOf("item1", "item2", "item3"), // Correct
            "Option C"                         // Correct
        )
        val maxScore = 100

        // Correct answers: 3 out of 4
        val expectedScore = (3.0 / 4 * 100).toInt() // 75

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedScore, result.getOrNull())
    }

    @Test
    fun `invoke should treat answers with wrong types as incorrect`() {
        // Arrange
        val questions = listOf(
            createMultipleChoiceQuestion("MCQ 1", "Option A"),
            createTrueFalseQuestion("TFQ 1", true),
            createOrderingQuestion("OQ 1", listOf("first", "second"))
        )
        val answers = listOf(
            "Option A",           // Correct
            "true",               // Incorrect type (String instead of Boolean)
            listOf("first", 2)    // Incorrect type inside list
        )
        val maxScore = 60

        // Correct answers: 1 out of 3
        val expectedScore = (1.0 / 3 * 60).toInt() // 20

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedScore, result.getOrNull())
    }

    @Test
    fun `invoke should handle empty question and answer lists`() {
        // Arrange
        val questions = emptyList<Question>()
        val answers = emptyList<Any>()
        val maxScore = 100

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        // Define behavior: 0 questions, 0 answers -> score 0
        // Alternatively, it could be undefined. Here, assuming score 0.
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `invoke should handle single question with correct answer`() {
        // Arrange
        val questions = listOf(
            createTrueFalseQuestion("TFQ 1", false)
        )
        val answers = listOf(
            false
        )
        val maxScore = 10

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull())
    }

    @Test
    fun `invoke should handle single question with incorrect answer`() {
        // Arrange
        val questions = listOf(
            createTrueFalseQuestion("TFQ 1", false)
        )
        val answers = listOf(
            true
        )
        val maxScore = 10

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `invoke should calculate score correctly with floating point precision`() {
        // Arrange
        val questions = listOf(
            createMultipleChoiceQuestion("MCQ 1", "Option A"),
            createMultipleChoiceQuestion("MCQ 2", "Option B"),
            createMultipleChoiceQuestion("MCQ 3", "Option C")
        )
        val answers = listOf(
            "Option A", // Correct
            "Option C", // Incorrect
            "Option B"  // Incorrect
        )
        val maxScore = 10

        // Correct answers: 1 out of 3
        // Expected score: (1 / 3) * 10 = 3.333... -> 3 after toInt()

        // Act
        val result = gradeQuizUseCase.invoke(questions, answers, maxScore)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull())
    }
}
