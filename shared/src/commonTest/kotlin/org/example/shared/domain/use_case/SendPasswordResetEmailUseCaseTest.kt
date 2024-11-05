package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.service.AuthService
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class SendPasswordResetEmailUseCaseTest {
    private lateinit var sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
    private lateinit var authService: AuthService

    @Before
    fun setUp() {
        authService = mockk(relaxed = true)
        sendPasswordResetEmailUseCase = SendPasswordResetEmailUseCase(authService)
    }

    @Test
    fun `invoke should call sendPasswordResetEmail on authService`() = runTest {
        // Arrange
        val email = "test@example.com"

        // Act
        sendPasswordResetEmailUseCase(email)

        // Assert
        coVerify { authService.sendPasswordResetEmail(email) }
    }

    @Test
    fun `invoke should return Result#success when sendPasswordResetEmail returns Result#success`() = runTest {
        // Arrange
        val email = "test@example.com"
        coEvery { authService.sendPasswordResetEmail(email) } returns Result.success(Unit)

        // Act
        val result = sendPasswordResetEmailUseCase(email)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should return Result#failure when sendPasswordResetEmail returns Result#failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val exception = Exception("An error occurred")
        coEvery { authService.sendPasswordResetEmail(email) } returns Result.failure(exception)

        // Act
        val result = sendPasswordResetEmailUseCase(email)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}