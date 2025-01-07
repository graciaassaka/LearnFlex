package org.example.shared.domain.use_case.lesson

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Question
import org.example.shared.domain.use_case.quiz.GenerateQuizUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateLessonQuizUseCaseTest {

    private lateinit var generateQuizUseCase: GenerateQuizUseCase
    private lateinit var generateLessonQuizUseCase: GenerateLessonQuizUseCase

    @BeforeTest
    fun setUp() {
        generateQuizUseCase = mockk()
        generateLessonQuizUseCase = GenerateLessonQuizUseCase(generateQuizUseCase)
    }

    @Test
    fun `invoke should call generateQuizUseCase with correct parameters`() = runTest {
        // Arrange
        val topic = "kotlin"
        val level = Level.BEGINNER
        val expectedNumberOfQuestions = GenerateLessonQuizUseCase.NUMBER_OF_QUESTIONS

        val mockFlow: Flow<Result<Question>> = flowOf()

        every { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) } returns mockFlow

        // Act
        val resultFlow = generateLessonQuizUseCase.invoke(topic, level)

        // Assert
        verify(exactly = 1) { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) }
        assertEquals(mockFlow, resultFlow)
    }

    @Test
    fun `invoke should emit the same results as generateQuizUseCase`() = runTest {
        // Arrange
        val topic = "kotlin"
        val level = Level.INTERMEDIATE
        val expectedNumberOfQuestions = GenerateLessonQuizUseCase.NUMBER_OF_QUESTIONS

        val mockQuestions = listOf(
            Result.success(Question.MultipleChoice("MCQ 1", "Answer1", listOf())),
            Result.failure<Question>(RuntimeException("Failed to generate"))
        )
        val mockFlow: Flow<Result<Question>> = flowOf(*mockQuestions.toTypedArray())

        every { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) } returns mockFlow

        // Act
        val resultFlow = generateLessonQuizUseCase.invoke(topic, level)
        val results = resultFlow.toList()

        // Assert
        assertEquals(mockQuestions, results)
        verify(exactly = 1) { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) }
    }

    @Test
    fun `invoke should handle generateQuizUseCase emitting multiple results`() = runTest {
        // Arrange
        val topic = "kotlin"
        val level = Level.ADVANCED
        val expectedNumberOfQuestions = GenerateLessonQuizUseCase.NUMBER_OF_QUESTIONS

        val mockQuestions = listOf(
            Result.success(Question.TrueFalse("TFQ 1", true)),
            Result.success(Question.Ordering("OQ 1", listOf("first", "second"))),
            Result.failure<Question>(RuntimeException("Partial failure"))
        )
        val mockFlow: Flow<Result<Question>> = flowOf(*mockQuestions.toTypedArray())

        every { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) } returns mockFlow

        // Act
        val resultFlow = generateLessonQuizUseCase.invoke(topic, level)
        val results = resultFlow.toList()

        // Assert
        assertEquals(mockQuestions, results)
        verify(exactly = 1) { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) }
    }
}
