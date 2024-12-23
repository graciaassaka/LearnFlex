package org.example.shared.domain.use_case.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SignInUseCaseTest {
    private lateinit var signInUseCase: SignInUseCase
    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        authClient = mockk<AuthClient>()
        signInUseCase = SignInUseCase(authClient)
    }


    @Test
    fun `SignInUseCase should call AuthService#signIn with the provided email and password`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authClient.signIn(any(), any()) } returns Result.success(Unit)

        // When
        signInUseCase(email, password)

        // Then
        coVerify(exactly = 1) { authClient.signIn(email, password) }
    }

    @Test
    fun `SignInUseCase should return success when AuthService#signIn returns success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authClient.signIn(any(), any()) } returns Result.success(Unit)

        // When
        val result = signInUseCase(email, password)

        // Then
        assert(result.isSuccess)
    }

    @Test
    fun `SignInUseCase should return failure with the exception when AuthService#signIn returns failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("An error occurred")

        coEvery { authClient.signIn(any(), any()) } returns Result.failure(exception)

        // When
        val result = signInUseCase(email, password)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}