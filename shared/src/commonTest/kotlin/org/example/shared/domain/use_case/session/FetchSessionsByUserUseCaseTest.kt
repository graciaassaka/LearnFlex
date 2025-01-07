package org.example.shared.domain.use_case.session

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchSessionsByUserUseCaseTest {
    private lateinit var useCase: FetchSessionsByUserUseCase
    private lateinit var repository: SessionRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = FetchSessionsByUserUseCase(repository)
    }

    @Test
    fun `invoke should return sessions flow when getAll succeeds`() = runTest {
        // Arrange
        val sessions = listOf(mockk<Session>())
        val sessionsFlow = flowOf(Result.success(sessions))
        every { repository.getAll(path) } returns sessionsFlow

        // Act
        val result = useCase(USER_ID)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(Result.success(sessions), result)
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val exception = RuntimeException("Get failed")
        val errorFlow = flowOf(Result.failure<List<Session>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val result = useCase(USER_ID)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    companion object {
        private const val USER_ID = "userId"
        private val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(USER_ID)
            .collection(Collection.SESSIONS)
            .build()
    }
}