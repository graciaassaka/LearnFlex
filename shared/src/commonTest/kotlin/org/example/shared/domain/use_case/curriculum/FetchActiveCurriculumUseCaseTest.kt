package org.example.shared.domain.use_case.curriculum

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Curriculum
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FetchActiveCurriculumUseCaseTest {
    private lateinit var fetchCurriculaByUserUseCase: FetchCurriculaByUserUseCase
    private lateinit var useCase: FetchActiveCurriculumUseCase

    @BeforeTest
    fun setUp() {
        fetchCurriculaByUserUseCase = mockk()
        useCase = FetchActiveCurriculumUseCase(fetchCurriculaByUserUseCase)
    }

    @Test
    fun `invoke should return the most recently updated curriculum when getAllCurriculaUseCase succeeds with non-empty list`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculum1 = mockk<Curriculum>()
        val curriculum2 = mockk<Curriculum>()
        val curriculum3 = mockk<Curriculum>()

        // Mock lastUpdated timestamps
        every { curriculum1.lastUpdated } returns 1000L
        every { curriculum2.lastUpdated } returns 2000L
        every { curriculum3.lastUpdated } returns 1500L

        val curriculaList = listOf(curriculum1, curriculum2, curriculum3)
        val expectedResult: Result<List<Curriculum>> = Result.success(curriculaList)
        coEvery { fetchCurriculaByUserUseCase.invoke(userId) } returns expectedResult

        // Act
        val result = useCase(userId)

        // Assert
        coVerify(exactly = 1) { fetchCurriculaByUserUseCase.invoke(userId) }
        assertEquals(Result.success(curriculum2), result)
    }

    @Test
    fun `invoke should return null when getAllCurriculaUseCase succeeds with empty list`() = runTest {
        // Arrange
        val userId = "userId"
        val emptyList: List<Curriculum> = emptyList()
        val expectedResult: Result<List<Curriculum>> = Result.success(emptyList)
        coEvery { fetchCurriculaByUserUseCase.invoke(userId) } returns expectedResult

        // Act
        val result = useCase(userId)

        // Assert
        coVerify(exactly = 1) { fetchCurriculaByUserUseCase.invoke(userId) }
        assertEquals(Result.success(null), result)
    }

    @Test
    fun `invoke should return error when getAllCurriculaUseCase fails`() = runTest {
        // Arrange
        val userId = "userId"
        val exception = RuntimeException("Failed to retrieve curricula")
        val expectedResult: Result<List<Curriculum>> = Result.failure(exception)
        coEvery { fetchCurriculaByUserUseCase.invoke(userId) } returns expectedResult

        // Act
        val result = useCase(userId)

        // Assert
        coVerify(exactly = 1) { fetchCurriculaByUserUseCase.invoke(userId) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when getAllCurriculaUseCase throws`() {
        // Arrange
        val userId = "userId"
        val exception = IllegalArgumentException("Invalid userId")
        coEvery { fetchCurriculaByUserUseCase.invoke(userId) } throws exception

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(userId).getOrThrow()
            }
        }
        coVerify(exactly = 1) { fetchCurriculaByUserUseCase.invoke(userId) }
    }
}
