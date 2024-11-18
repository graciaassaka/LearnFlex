package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.repository.Repository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetUserStyleUseCaseTest {
    private lateinit var getUserStyleUseCase: GetUserStyleUseCase
    private lateinit var styleRepos: Repository<LearningStyle>

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
        coVerify(exactly = 1) { styleRepos.get(any()) }
    }

    @Test
    fun `getUserStyleUseCase should return Result#success when getLearningStyle is successful`() = runTest {
        // Given
        val expected = mockk<LearningStyle>()
        coEvery { styleRepos.get(any()) } returns flowOf(Result.success(expected))

        // When
        val result = getUserStyleUseCase("userId").single()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getUserStyleUseCase should return Result#failure when getLearningStyle is failed`() = runTest {
        // Given
        val expected = Exception("Error")
        coEvery { styleRepos.get(any()) } returns flowOf(Result.failure(expected))

        // When
        val result = getUserStyleUseCase("userId").single()

        // Then
        assertTrue(result.isFailure)
        assertEquals(expected, result.exceptionOrNull())
    }
}