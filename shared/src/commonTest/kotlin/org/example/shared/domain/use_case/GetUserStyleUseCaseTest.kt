package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.data.model.StyleResult
import org.example.shared.domain.repository.LearningStyleRepos
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetUserStyleUseCaseTest {
    private lateinit var getUserStyleUseCase: GetUserStyleUseCase
    private lateinit var styleRepos: LearningStyleRepos

    @Before
    fun setUp() {
        styleRepos = mockk(relaxed = true)
        getUserStyleUseCase = GetUserStyleUseCase(styleRepos)
    }

    @Test
    fun `getUserStyleUseCase should call getLearningStyle from styleRepos`() = runTest{
        // When
        getUserStyleUseCase("userId")

        // Then
        coVerify(exactly = 1) { styleRepos.getLearningStyle(any()) }
    }

    @Test
    fun `getUserStyleUseCase should return Result#success when getLearningStyle is successful`() = runTest {
        // Given
        val expected = mockk<StyleResult>()
        coEvery { styleRepos.getLearningStyle(any()) } returns Result.success(expected)

        // When
        val result = getUserStyleUseCase("userId")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getUserStyleUseCase should return Result#failure when getLearningStyle is failed`() = runTest {
        // Given
        val expected = Exception("Error")
        coEvery { styleRepos.getLearningStyle(any()) } returns Result.failure(expected)

        // When
        val result = getUserStyleUseCase("userId")

        // Then
        assertTrue(result.isFailure)
        assertEquals(expected, result.exceptionOrNull())
    }
}