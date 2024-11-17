package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.StyleResult
import org.example.shared.domain.data_source.LearningStyleRemoteDataSource
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SetUserStyleUseCaseTest {
    private lateinit var setUserStyleUseCase: SetUserStyleUseCase
    private lateinit var styleRepos: LearningStyleRemoteDataSource

    @Before
    fun setUp() {
        styleRepos = mockk(relaxed = true)
        setUserStyleUseCase = SetUserStyleUseCase(styleRepos)
    }

    @Test
    fun `setUserStyleUseCase should call setLearningStyle from styleRepos`() = runTest {
        // Given
        val style = mockk<StyleResult>()

        // When
        setUserStyleUseCase.invoke("userId", style)

        // Then
        coVerify(exactly = 1) { styleRepos.setLearningStyle(any(), any()) }
    }

    @Test
    fun `setUserStyleUseCase should return Result#success when setLearningStyle is successful`() = runTest {
        // Given
        val style = mockk<StyleResult>()
        coEvery { styleRepos.setLearningStyle(any(), any()) } returns Result.success(Unit)

        // When
        val result = setUserStyleUseCase.invoke("userId", style)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `setUserStyleUseCase should return Result#failure when setLearningStyle is failed`() = runTest {
        // Given
        val style = mockk<StyleResult>()
        val expected = Exception("Error")
        coEvery { styleRepos.setLearningStyle(any(), any()) } returns Result.failure(expected)

        // When
        val result = setUserStyleUseCase.invoke("userId", style)

        // Then
        assertTrue(result.isFailure)
        assertEquals(expected, result.exceptionOrNull())
    }
}