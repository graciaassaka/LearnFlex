package org.example.shared.domain.use_case

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.example.shared.data.model.LearningPreferences
import org.example.shared.data.model.StyleQuestionnaire
import org.example.shared.domain.service.StyleQuizService
import org.junit.Before
import org.junit.Test

class GetStyleQuestionnaireUseCaseTest {
    private lateinit var getStyleQuestionnaireUseCase: GetStyleQuestionnaireUseCase
    private lateinit var styleQuizService: StyleQuizService

    @Before
    fun setUp() {
        styleQuizService = mockk<StyleQuizService>(relaxed = true)
        getStyleQuestionnaireUseCase = GetStyleQuestionnaireUseCase(styleQuizService)
    }

    @Test
    fun `getStyleQuestionnaire should call generateQuiz from styleQuizService`() = runTest {
        // Arrange
        val preferences = mockk<LearningPreferences>()

        // Act
        getStyleQuestionnaireUseCase(preferences)

        // Assert
        coVerify(exactly = 1) { styleQuizService.generateQuiz(preferences) }
    }

    @Test
    fun `getStyleQuestionnaire should return Result#success when styleQuizService#generateQuiz returns a questionnaire`() = runTest {
        // Arrange
        val preferences = mockk<LearningPreferences>()
        val questionnaire = mockk<StyleQuestionnaire>()
        coEvery { styleQuizService.generateQuiz(preferences) } returns Result.success(questionnaire)

        // Act
        val result = getStyleQuestionnaireUseCase(preferences)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == questionnaire)
    }

    @Test
    fun `getStyleQuestionnaire should return Result#failure when styleQuizService#generateQuiz throws an exception`() = runTest {
        // Arrange
        val preferences = mockk<LearningPreferences>()
        val exception = Exception("An error occurred")
        coEvery { styleQuizService.generateQuiz(preferences) } returns Result.failure(exception)

        // Act
        val result = getStyleQuestionnaireUseCase(preferences)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}