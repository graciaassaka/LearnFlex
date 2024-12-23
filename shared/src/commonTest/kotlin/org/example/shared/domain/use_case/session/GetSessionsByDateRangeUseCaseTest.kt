package org.example.shared.domain.use_case.session

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test

class GetSessionsByDateRangeUseCaseTest {
    private lateinit var useCase: GetSessionsByDateRangeUseCase
    private lateinit var repository: SessionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSessionsByDateRangeUseCase(repository)
    }

    @Test
    fun `invoke should return sessions flow for date range when succeeds`() = runTest {
        // Arrange
        val startTime = 1000L
        val endTime = 2000L
        val sessions = listOf(mockk<Session>(), mockk())

        coEvery { repository.queryByDateRange(startTime, endTime) } returns Result.success(sessions)

        // Act
        val result = useCase(startTime, endTime)

        // Assert
        coVerify(exactly = 1) { repository.queryByDateRange(startTime, endTime) }
        assertEquals(sessions, result.getOrNull())
    }

    @Test
    fun `invoke should return error flow when query fails`() = runTest {
        // Arrange
        val startTime = 1000L
        val endTime = 2000L
        val exception = RuntimeException("Failed to query sessions")

        coEvery { repository.queryByDateRange(startTime, endTime) } returns Result.failure(exception)

        // Act
        val result = useCase(startTime, endTime)

        // Assert
        coVerify(exactly = 1) { repository.queryByDateRange(startTime, endTime) }
        assertEquals(exception, result.exceptionOrNull())
    }
}