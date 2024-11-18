package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.repository.Repository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class CreateUserStyleUseCaseTest {
    private lateinit var createUserStyleUseCase: CreateUserStyleUseCase
    private lateinit var styleRepos: Repository<LearningStyle>

    @Before
    fun setUp() {
        styleRepos = mockk(relaxed = true)
        createUserStyleUseCase = CreateUserStyleUseCase(styleRepos)
    }

    @Test
    fun `setUserStyleUseCase should call create from styleRepos`() = runTest {
        // Given
        val style = mockk<LearningStyle>()

        // When
        createUserStyleUseCase.invoke(style)

        // Then
        coVerify(exactly = 1) { styleRepos.create(any()) }
    }

    @Test
    fun `setUserStyleUseCase should return Result#success when create is successful`() = runTest {
        // Given
        val style = mockk<LearningStyle>()
        coEvery { styleRepos.create(any()) } returns Result.success(Unit)

        // When
        val result = createUserStyleUseCase.invoke(style)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `setUserStyleUseCase should return Result#failure when create is failed`() = runTest {
        // Given
        val style = mockk<LearningStyle>()
        val expected = Exception("Error")
        coEvery { styleRepos.create(any()) } returns Result.failure(expected)

        // When
        val result = createUserStyleUseCase.invoke(style)

        // Then
        assertTrue(result.isFailure)
        assertEquals(expected, result.exceptionOrNull())
    }
}