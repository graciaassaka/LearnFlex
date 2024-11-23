package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.service.StyleQuizClient
import org.junit.Before
import kotlin.test.Test

class GetLearningStyleUseCaseTest {
    private lateinit var getStyleResultUseCase: GetStyleResultUseCase
    private lateinit var styleQuizClient: StyleQuizClient

    @Before
    fun setUp() {
        styleQuizClient = mockk<StyleQuizClient>(relaxed = true)
        getStyleResultUseCase = GetStyleResultUseCase(styleQuizClient)
    }

    @Test
    fun `getStyleResult should call evaluateResponses from styleQuizService`() = runTest {
        // Act
        getStyleResultUseCase(mockk())

        // Assert
        coVerify(exactly = 1) { styleQuizClient.evaluateResponses(any()) }
    }

    @Test
    fun `getStyleResult should return Result#success when styleQuizService#evaluateResponses returns StyleResult`() = runTest {
        // Arrange
        val learningStyle = mockk<LearningStyle>()
        coEvery { styleQuizClient.evaluateResponses(any()) } returns Result.success(learningStyle)

        // Act
        val result = getStyleResultUseCase(mockk())

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == learningStyle)
    }

    @Test
    fun `getStyleResult should return Result#failure when styleQuizService#evaluateResponses throws an exception`() = runTest {
        // Arrange
        val exception = Exception("An error occurred")
        coEvery { styleQuizClient.evaluateResponses(any()) } returns Result.failure(exception)

        // Act
        val result = getStyleResultUseCase(mockk())

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}