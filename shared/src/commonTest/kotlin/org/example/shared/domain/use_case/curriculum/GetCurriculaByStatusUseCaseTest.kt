package org.example.shared.domain.use_case.curriculum

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.ContentStatus
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetCurriculaByStatusUseCaseTest {
    private lateinit var getCurriculaByStatusUseCase: GetCurriculaByStatusUseCase
    private lateinit var repository: CurriculumRepository

    @Before
    fun setUp() {
        repository = mockk()
        getCurriculaByStatusUseCase = GetCurriculaByStatusUseCase(repository)
    }

    @Test
    fun `invoke should return success with filtered curricula list`() = runTest {
        // Arrange
        val status = ContentStatus.UNFINISHED
        val curricula = List(3) { mockk<Curriculum>(relaxed = true) }

        coEvery { repository.getByStatus(status) } returns Result.success(curricula)

        // Act
        val result = getCurriculaByStatusUseCase(status)

        // Assert
        coVerify(exactly = 1) { repository.getByStatus(status) }
        assert(result.isSuccess)
        assertEquals(curricula, result.getOrNull())
    }

    @Test
    fun `invoke should return success with empty list`() = runTest {
        // Arrange
        val status = ContentStatus.UNFINISHED
        val curricula = emptyList<Curriculum>()

        coEvery { repository.getByStatus(status) } returns Result.success(curricula)

        // Act
        val result = getCurriculaByStatusUseCase(status)

        // Assert
        coVerify(exactly = 1) { repository.getByStatus(status) }
        assert(result.isSuccess)
        assertEquals(curricula, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val status = ContentStatus.UNFINISHED
        val exception = RuntimeException("Failed to get curricula")

        coEvery { repository.getByStatus(status) } returns Result.failure(exception)

        // Act
        val result = getCurriculaByStatusUseCase(status)

        // Assert
        coVerify(exactly = 1) { repository.getByStatus(status) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}