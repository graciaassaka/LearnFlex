package org.example.shared.domain.use_case.session

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UploadSessionUseCaseTest {
    private lateinit var useCase: UploadSessionUseCase
    private lateinit var repository: SessionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UploadSessionUseCase(repository)
    }

    @Test
    fun `upload should return success when succeeds`() = runTest {
        // Arrange
        val session = mockk<Session> {
            every { id } returns SESSION_ID
        }
        coEvery { repository.insert(session, path, any()) } returns Result.success(Unit)

        // Act
        val result = useCase(session, USER_ID)

        // Assert
        coVerify(exactly = 1) { repository.insert(session, path, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `upload should return failure when fails`() = runTest {
        // Arrange
        val session = mockk<Session> {
            every { id } returns SESSION_ID
        }
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insert(session, path, any()) } returns Result.failure(exception)

        // Act
        val result = useCase(session, USER_ID)

        // Assert
        coVerify(exactly = 1) { repository.insert(session, path, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private const val USER_ID = "userId"
        private const val SESSION_ID = "sessionId"
        private val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(USER_ID)
            .collection(Collection.SESSIONS)
            .document(SESSION_ID)
            .build()
    }
}