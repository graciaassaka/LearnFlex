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

class GetActiveCurriculumUseCaseTest {
    private lateinit var getAllCurriculaUseCase: GetAllCurriculaUseCase
    private lateinit var useCase: GetActiveCurriculumUseCase

    @BeforeTest
    fun setUp() {
        getAllCurriculaUseCase = mockk()
        useCase = GetActiveCurriculumUseCase(getAllCurriculaUseCase)
    }

    @Test
    fun `invoke should return the most recently updated curriculum when getAllCurriculaUseCase succeeds with non-empty list`() = runTest {
        // Arrange
        val path = "some/path"
        val curriculum1 = mockk<Curriculum>()
        val curriculum2 = mockk<Curriculum>()
        val curriculum3 = mockk<Curriculum>()

        // Mock lastUpdated timestamps
        every { curriculum1.lastUpdated } returns 1000L
        every { curriculum2.lastUpdated } returns 2000L
        every { curriculum3.lastUpdated } returns 1500L

        val curriculaList = listOf(curriculum1, curriculum2, curriculum3)
        val expectedResult: Result<List<Curriculum>> = Result.success(curriculaList)
        coEvery { getAllCurriculaUseCase.invoke(path) } returns expectedResult

        // Act
        val result = useCase(path)

        // Assert
        coVerify(exactly = 1) { getAllCurriculaUseCase.invoke(path) }
        assertEquals(Result.success(curriculum2), result)
    }

    @Test
    fun `invoke should return null when getAllCurriculaUseCase succeeds with empty list`() = runTest {
        // Arrange
        val path = "some/path"
        val emptyList: List<Curriculum> = emptyList()
        val expectedResult: Result<List<Curriculum>> = Result.success(emptyList)
        coEvery { getAllCurriculaUseCase.invoke(path) } returns expectedResult

        // Act
        val result = useCase(path)

        // Assert
        coVerify(exactly = 1) { getAllCurriculaUseCase.invoke(path) }
        assertEquals(Result.success(null), result)
    }

    @Test
    fun `invoke should return error when getAllCurriculaUseCase fails`() = runTest {
        // Arrange
        val path = "some/path"
        val exception = RuntimeException("Failed to retrieve curricula")
        val expectedResult: Result<List<Curriculum>> = Result.failure(exception)
        coEvery { getAllCurriculaUseCase.invoke(path) } returns expectedResult

        // Act
        val result = useCase(path)

        // Assert
        coVerify(exactly = 1) { getAllCurriculaUseCase.invoke(path) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when getAllCurriculaUseCase throws`() {
        // Arrange
        val path = "some/path"
        val exception = IllegalArgumentException("Invalid path")
        coEvery { getAllCurriculaUseCase.invoke(path) } throws exception

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(path).getOrThrow()
            }
        }
        coVerify(exactly = 1) { getAllCurriculaUseCase.invoke(path) }
    }
}
