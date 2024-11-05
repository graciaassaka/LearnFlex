package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.service.AuthService
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SendVerificationEmailUseCaseTest {
    private lateinit var sendVerificationEmailUseCase: SendVerificationEmailUseCase
    private lateinit var authService: AuthService

    @Before
    fun setUp() {
        authService = mockk<AuthService>()
        sendVerificationEmailUseCase = SendVerificationEmailUseCase(authService)
    }

    @Test
    fun `SendVerificationEmailUseCase should call AuthService#sendVerificationEmail`() = runTest {
        // Given
        coEvery { authService.sendEmailVerification() } returns Result.success(Unit)

        // When
        sendVerificationEmailUseCase()

        // Then
        coVerify(exactly = 1) { authService.sendEmailVerification() }
    }

    @Test
    fun `SendVerificationEmailUseCase should return success when AuthService#sendVerificationEmail returns success`() =
        runTest {
            // Given
            coEvery { authService.sendEmailVerification() } returns Result.success(Unit)

            // When
            val result = sendVerificationEmailUseCase()

            // Then
            assert(result.isSuccess)
        }

    @Test
    fun `SendVerificationEmailUseCase should return failure with the exception when AuthService#sendVerificationEmail returns failure`() =
        runTest {
            // Given
            val exception = Exception("An error occurred")
            coEvery { authService.sendEmailVerification() } returns Result.failure(exception)

            // When
            val result = sendVerificationEmailUseCase()

            // Then
            assert(result.isFailure)
            assert(result.exceptionOrNull() == exception)
        }
}