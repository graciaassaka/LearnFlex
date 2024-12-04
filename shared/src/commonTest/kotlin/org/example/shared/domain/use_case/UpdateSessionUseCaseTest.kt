package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Session
import org.example.shared.domain.repository.SessionRepository
import org.junit.Before
import org.junit.Test

class UpdateSessionUseCaseTest {
    private lateinit var useCase: UpdateSessionUseCase
    private lateinit var repository: SessionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdateSessionUseCase(repository)
    }

    @Test
    fun `update should return success when succeeds`() = runTest {
        // Arrange
        val path = "test/path"
        val session = mockk<Session>(relaxed = true)
        coEvery { repository.update(path, session) } returns Result.success(Unit)

        // Act
        val result = useCase(path, session)

        // Assert
        coVerify(exactly = 1) { repository.update(path, session) }
        assert(result.isSuccess)
    }

    @Test
    fun `update should return failure when fails`() = runTest {
        // Arrange
        val path = "test/path"
        val session = mockk<Session>(relaxed = true)
        val exception = RuntimeException("Update failed")
        coEvery { repository.update(path, session) } returns Result.failure(exception)

        // Act
        val result = useCase(path, session)

        // Assert
        coVerify(exactly = 1) { repository.update(path, session) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
