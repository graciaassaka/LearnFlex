package org.example.shared.domain.use_case.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.StyleQuizGenerator
import org.example.shared.domain.model.Profile
import org.junit.Before
import kotlin.test.Test

class GetLearningStyleUseCaseTest {
    private lateinit var getStyleResultUseCase: GetStyleResultUseCase
    private lateinit var styleQuizGenerator: StyleQuizGenerator

    @Before
    fun setUp() {
        styleQuizGenerator = mockk<StyleQuizGenerator>(relaxed = true)
        getStyleResultUseCase = GetStyleResultUseCase(styleQuizGenerator)
    }

    @Test
    fun `getStyleResult should call evaluateResponses from styleQuizService`() = runTest {
        // Act
        getStyleResultUseCase(mockk())

        // Assert
        coVerify(exactly = 1) { styleQuizGenerator.evaluateResponses(any()) }
    }

    @Test
    fun `getStyleResult should return Result#success when styleQuizService#evaluateResponses returns StyleResult`() = runTest {
        // Arrange
        val learningStyle = mockk<Profile.LearningStyle>()
        coEvery { styleQuizGenerator.evaluateResponses(any()) } returns Result.success(learningStyle)

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
        coEvery { styleQuizGenerator.evaluateResponses(any()) } returns Result.failure(exception)

        // Act
        val result = getStyleResultUseCase(mockk())

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}