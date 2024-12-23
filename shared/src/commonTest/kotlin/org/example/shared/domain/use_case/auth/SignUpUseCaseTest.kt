package org.example.shared.domain.use_case.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
class SignUpUseCaseTest {
    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        authClient = mockk<AuthClient>()
        signUpUseCase = SignUpUseCase(authClient)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `SignUpUseCase should call AuthService#signUp with the provided email and password`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authClient.signUp(any(), any()) } returns Result.success(Unit)

        // When
        signUpUseCase(email, password)

        // Then
        coVerify(exactly = 1) { authClient.signUp(email, password) }
    }

    @Test
    fun `SignUpUseCase should return success when AuthService#signUp returns success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authClient.signUp(any(), any()) } returns Result.success(Unit)

        // When
        val result = signUpUseCase(email, password)

        // Then
        assert(result.isSuccess)
    }

    @Test
    fun `SignUpUseCase should return failure with the exception when AuthService#signUp returns failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("An error occurred")

        coEvery { authClient.signUp(any(), any()) } returns Result.failure(exception)

        // When
        val result = signUpUseCase(email, password)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}