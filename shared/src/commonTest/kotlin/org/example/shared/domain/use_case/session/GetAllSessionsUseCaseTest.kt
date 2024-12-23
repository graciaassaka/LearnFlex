package org.example.shared.domain.use_case.session

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetAllSessionsUseCaseTest {
    private lateinit var useCase: GetAllSessionsUseCase
    private lateinit var repository: SessionRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = GetAllSessionsUseCase(repository)
    }

    @Test
    fun `invoke should return sessions flow when getAll succeeds`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildSessionPath("userId")
        val sessions = listOf(mockk<Session>())
        val sessionsFlow = flowOf(Result.success(sessions))
        every { repository.getAll(path) } returns sessionsFlow

        // Act
        val emissions = mutableListOf<Result<List<Session>>>()
        useCase(path).collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(1, emissions.size)
        assertEquals(Result.success(sessions), emissions.first())
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildSessionPath("userId")
        val exception = RuntimeException("Get failed")
        val errorFlow = flowOf(Result.failure<List<Session>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val emissions = mutableListOf<Result<List<Session>>>()
        useCase(path).collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(1, emissions.size)
        assert(emissions.first().isFailure)
        assertEquals(exception, emissions.first().exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when path does not end with SESSIONS`() {
        // Arrange
        val path = FirestorePathBuilder().buildSessionPath("userId") + "/extra"

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(path).first().getOrThrow()
            }
        }
    }
}