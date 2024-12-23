package org.example.shared.domain.use_case.session

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
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
        val path = "test/path"
        val session = mockk<Session>(relaxed = true)
        coEvery { repository.insert(path, session, any()) } returns Result.success(Unit)

        // Act
        val result = useCase(path, session)

        // Assert
        coVerify(exactly = 1) { repository.insert(path, session, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `upload should return failure when fails`() = runTest {
        // Arrange
        val path = "test/path"
        val session = mockk<Session>(relaxed = true)
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insert(path, session, any()) } returns Result.failure(exception)

        // Act
        val result = useCase(path, session)

        // Assert
        coVerify(exactly = 1) { repository.insert(path, session, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}