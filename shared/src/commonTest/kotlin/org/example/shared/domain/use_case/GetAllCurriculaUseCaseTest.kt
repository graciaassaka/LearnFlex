package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAllCurriculaUseCaseTest {
    private lateinit var getAllCurriculaUseCase: GetAllCurriculaUseCase
    private lateinit var repository: CurriculumRepository

    @Before
    fun setUp() {
        repository = mockk()
        getAllCurriculaUseCase = GetAllCurriculaUseCase(repository)
    }

    @Test
    fun `invoke should return success with curricula list`() = runTest {
        // Arrange
        val path = "test/path"
        val curricula = List(3) { mockk<Curriculum>(relaxed = true) }
        val flow = flowOf(Result.success(curricula))
        every { repository.getAll(path) } returns flow

        // Act
        val result = getAllCurriculaUseCase(path).first()

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assert(result.isSuccess)
        assertEquals(curricula, result.getOrNull())
    }

    @Test
    fun `invoke should return success with empty list`() = runTest {
        // Arrange
        val path = "test/path"
        val curricula = emptyList<Curriculum>()
        val flow = flowOf(Result.success(curricula))
        every { repository.getAll(path) } returns flow

        // Act
        val result = getAllCurriculaUseCase(path).first()

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assert(result.isSuccess)
        assertEquals(curricula, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val path = "test/path"
        val exception = RuntimeException("Failed to get curricula")
        val flow = flowOf(Result.failure<List<Curriculum>>(exception))
        every { repository.getAll(path) } returns flow

        // Act
        val result = getAllCurriculaUseCase(path).first()

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}