package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.junit.Before
import kotlin.test.Test

class GetSessionByDateRangeUseCaseTest {
    private lateinit var useCase: GetSessionByDateRangeUseCase
    private lateinit var repository: SessionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSessionByDateRangeUseCase(repository)
    }

    @Test
    fun `invoke should return sessions flow for date range when succeeds`() = runTest {
        // Arrange
        val startTime = 1000L
        val endTime = 2000L
        val sessions = listOf(mockk<Session>(), mockk())
        val sessionsFlow = flowOf(Result.success(sessions))
        every { repository.queryByDateRange(startTime, endTime) } returns sessionsFlow

        // Act
        val result = useCase(startTime, endTime)

        // Assert
        verify(exactly = 1) { repository.queryByDateRange(startTime, endTime) }
        assertEquals(sessionsFlow, result)
    }

    @Test
    fun `invoke should return error flow when query fails`() {
        // Arrange
        val startTime = 1000L
        val endTime = 2000L
        val exception = RuntimeException("Failed to query sessions")
        val errorFlow = flowOf(Result.failure<List<Session>>(exception))
        every { repository.queryByDateRange(startTime, endTime) } returns errorFlow

        // Act
        val result = useCase(startTime, endTime)

        // Assert
        verify(exactly = 1) { repository.queryByDateRange(startTime, endTime) }
        assertEquals(errorFlow, result)
    }
}