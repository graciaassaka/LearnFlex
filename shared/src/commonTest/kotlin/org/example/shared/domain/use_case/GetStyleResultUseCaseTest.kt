package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.StyleResult
import org.example.shared.domain.service.StyleQuizService
import org.junit.Before
import kotlin.test.Test

class GetStyleResultUseCaseTest {
    private lateinit var getStyleResultUseCase: GetStyleResultUseCase
    private lateinit var styleQuizService: StyleQuizService

    @Before
    fun setUp() {
        styleQuizService = mockk<StyleQuizService>(relaxed = true)
        getStyleResultUseCase = GetStyleResultUseCase(styleQuizService)
    }

    @Test
    fun `getStyleResult should call evaluateResponses from styleQuizService`() = runTest {
        // Act
        getStyleResultUseCase(mockk())

        // Assert
        coVerify(exactly = 1) { styleQuizService.evaluateResponses(any()) }
    }

    @Test
    fun `getStyleResult should return Result#success when styleQuizService#evaluateResponses returns StyleResult`() = runTest {
        // Arrange
        val styleResult = mockk<StyleResult>()
        coEvery { styleQuizService.evaluateResponses(any()) } returns Result.success(styleResult)

        // Act
        val result = getStyleResultUseCase(mockk())

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == styleResult)
    }

    @Test
    fun `getStyleResult should return Result#failure when styleQuizService#evaluateResponses throws an exception`() = runTest {
        // Arrange
        val exception = Exception("An error occurred")
        coEvery { styleQuizService.evaluateResponses(any()) } returns Result.failure(exception)

        // Act
        val result = getStyleResultUseCase(mockk())

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}