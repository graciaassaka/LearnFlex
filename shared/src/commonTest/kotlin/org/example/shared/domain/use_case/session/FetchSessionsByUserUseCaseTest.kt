package org.example.shared.domain.use_case.session

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
        val sessionsResult = Result.success(sessions)
        coEvery { repository.getAll(path) } returns sessionsResult

        // Act
        val result = useCase(USER_ID)

        // Assert
        coVerify(exactly = 1) { repository.getAll(path) }
        assertEquals(Result.success(sessions), result)
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val exception = RuntimeException("Get failed")
        val errorResult = Result.failure<List<Session>>(exception)
        coEvery { repository.getAll(path) } returns errorResult

        // Act
        val result = useCase(USER_ID)

        // Assert
        coVerify(exactly = 1) { repository.getAll(path) }
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