package org.example.shared.domain.use_case.module

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
class GenerateModuleQuizUseCaseTest {

    private lateinit var generateQuizUseCase: GenerateQuizUseCase
    private lateinit var generateModuleQuizUseCase: GenerateModuleQuizUseCase

    @BeforeTest
    fun setUp() {
        generateQuizUseCase = mockk()
        generateModuleQuizUseCase = GenerateModuleQuizUseCase(generateQuizUseCase)
    }

    @Test
    fun `invoke should call generateQuizUseCase with correct parameters`() = runTest {
        // Arrange
        val topic = "math"
        val level = Level.INTERMEDIATE
        val expectedNumberOfQuestions = GenerateModuleQuizUseCase.NUMBER_OF_QUESTIONS

        val mockFlow: Flow<Result<Question>> = flowOf()

        every { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) } returns mockFlow

        // Act
        val resultFlow = generateModuleQuizUseCase.invoke(topic, level)

        // Assert
        verify(exactly = 1) { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) }
        assertEquals(mockFlow, resultFlow)
    }

    @Test
    fun `invoke should emit the same results as generateQuizUseCase`() = runTest {
        // Arrange
        val topic = "math"
        val level = Level.ADVANCED
        val expectedNumberOfQuestions = GenerateModuleQuizUseCase.NUMBER_OF_QUESTIONS

        val mockQuestions = listOf(
            Result.success(Question.Ordering("OQ 1", listOf("alpha", "beta"))),
            Result.success(Question.MultipleChoice("MCQ 1", "Answer1", listOf())),
            Result.failure<Question>(RuntimeException("Generator failed"))
        )
        val mockFlow: Flow<Result<Question>> = flowOf(*mockQuestions.toTypedArray())

        every { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) } returns mockFlow

        // Act
        val resultFlow = generateModuleQuizUseCase.invoke(topic, level)
        val results = resultFlow.toList()

        // Assert
        assertEquals(mockQuestions, results)
        verify(exactly = 1) { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) }
    }

    @Test
    fun `invoke should handle generateQuizUseCase emitting multiple results`() = runTest {
        // Arrange
        val topic = "math"
        val level = Level.BEGINNER
        val expectedNumberOfQuestions = GenerateModuleQuizUseCase.NUMBER_OF_QUESTIONS

        val mockQuestions = listOf(
            Result.success(Question.TrueFalse("TFQ 1", false)),
            Result.success(Question.Ordering("OQ 2", listOf("gamma", "delta"))),
            Result.failure<Question>(RuntimeException("Partial failure")),
            Result.success(Question.MultipleChoice("MCQ 2", "Answer2", listOf()))
        )
        val mockFlow: Flow<Result<Question>> = flowOf(*mockQuestions.toTypedArray())

        every { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) } returns mockFlow

        // Act
        val resultFlow = generateModuleQuizUseCase.invoke(topic, level)
        val results = resultFlow.toList()

        // Assert
        assertEquals(mockQuestions, results)
        verify(exactly = 1) { generateQuizUseCase.invoke(topic, level, expectedNumberOfQuestions) }
    }
}
