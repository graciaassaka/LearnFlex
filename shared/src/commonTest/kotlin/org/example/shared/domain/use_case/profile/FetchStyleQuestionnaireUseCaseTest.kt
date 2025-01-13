package org.example.shared.domain.use_case.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.model.Profile
import org.junit.Before
import org.junit.Test

class FetchStyleQuestionnaireUseCaseTest {
    private lateinit var fetchStyleQuestionsUseCase: FetchStyleQuestionsUseCase
    private lateinit var styleQuizGeneratorClient: StyleQuizGeneratorClient

    @Before
    fun setUp() {
        styleQuizGeneratorClient = mockk<StyleQuizGeneratorClient>(relaxed = true)
        fetchStyleQuestionsUseCase = FetchStyleQuestionsUseCase(styleQuizGeneratorClient)
    }

    @Test
    fun `getStyleQuestionnaire should call generateQuiz from styleQuizService`() = runTest {
        // Arrange
        val preferences = mockk<Profile.LearningPreferences>()

        // Act
        fetchStyleQuestionsUseCase(preferences)

        // Assert
        coVerify(exactly = 1) { styleQuizGeneratorClient.streamQuestions(preferences, FetchStyleQuestionsUseCase.NUMBER_OF_QUESTIONS) }
    }

    @Test
    fun `getStyleQuestionnaire should return Result#success when styleQuizService#generateQuiz returns a questionnaire`() =
        runTest {
            // Arrange
            val preferences = mockk<Profile.LearningPreferences>()
            val question = mockk<StyleQuizGeneratorClient.StyleQuestion>()
            coEvery { styleQuizGeneratorClient.streamQuestions(preferences, 1) } returns flowOf(Result.success(question))

            // Act
            val result = mutableListOf<Result<StyleQuizGeneratorClient.StyleQuestion>>()
            fetchStyleQuestionsUseCase(preferences, 1).collect(result::add)

            // Assert
            assert(result.size == 1)
            assert(result[0].getOrNull() == question)
        }

    @Test
    fun `getStyleQuestionnaire should return Result#failure when styleQuizService#generateQuiz throws an exception`() =
        runTest {
            // Arrange
            val preferences = mockk<Profile.LearningPreferences>()
            val exception = Exception("An error occurred")
            coEvery {
                styleQuizGeneratorClient.streamQuestions(
                    preferences,
                    FetchStyleQuestionsUseCase.NUMBER_OF_QUESTIONS
                )
            } returns flowOf(
                Result.failure(exception)
            )

            // Act
            val result = mutableListOf<Result<StyleQuizGeneratorClient.StyleQuestion>>()
            fetchStyleQuestionsUseCase(preferences).collect(result::add)

            // Assert
            assert(result.size == 1)
            assert(result[0].exceptionOrNull() == exception)
        }
}