package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAllSessionsUseCaseTest {
    private lateinit var useCase: GetAllSessionsUseCase
    private lateinit var repository: SessionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAllSessionsUseCase(repository)
    }

    @Test
    fun `getAll should return sessions flow when succeeds`() = runTest {
        // Arrange
        val path = "test/path"
        val sessions = listOf(mockk<Session>(), mockk())
        val sessionsFlow = flowOf(Result.success(sessions))
        every { repository.getAll(path) } returns sessionsFlow

        // Act
        val result = useCase(path).single()

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertTrue(result.isSuccess)
        assertEquals(sessions, result.getOrNull())
    }

    @Test
    fun `getAll should return error flow when fails`() = runTest {
        // Arrange
        val path = "test/path"
        val exception = RuntimeException("Failed to get sessions")
        val errorFlow = flowOf(Result.failure<List<Session>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val result = useCase(path).single()

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}