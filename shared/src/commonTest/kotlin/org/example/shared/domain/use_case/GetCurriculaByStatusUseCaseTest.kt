package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.CurriculumStatus
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
        val status = CurriculumStatus.IN_PROGRESS
        val curricula = List(3) { mockk<Curriculum>(relaxed = true) }
        val flow = flowOf(Result.success(curricula))
        every { repository.getByStatus(status) } returns flow

        // Act
        val result = getCurriculaByStatusUseCase(status).first()

        // Assert
        verify(exactly = 1) { repository.getByStatus(status) }
        assert(result.isSuccess)
        assertEquals(curricula, result.getOrNull())
    }

    @Test
    fun `invoke should return success with empty list`() = runTest {
        // Arrange
        val status = CurriculumStatus.IN_PROGRESS
        val curricula = emptyList<Curriculum>()
        val flow = flowOf(Result.success(curricula))
        every { repository.getByStatus(status) } returns flow

        // Act
        val result = getCurriculaByStatusUseCase(status).first()

        // Assert
        verify(exactly = 1) { repository.getByStatus(status) }
        assert(result.isSuccess)
        assertEquals(curricula, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val status = CurriculumStatus.IN_PROGRESS
        val exception = RuntimeException("Failed to get curricula")
        val flow = flowOf(Result.failure<List<Curriculum>>(exception))
        every { repository.getByStatus(status) } returns flow

        // Act
        val result = getCurriculaByStatusUseCase(status).first()

        // Assert
        verify(exactly = 1) { repository.getByStatus(status) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}