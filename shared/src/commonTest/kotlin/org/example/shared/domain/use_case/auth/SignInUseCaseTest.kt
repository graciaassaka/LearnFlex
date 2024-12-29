package org.example.shared.domain.use_case.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs

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
    fun `SignInUseCase should call AuthService#signIn with the provided email and password and AuthService#getUserData`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authClient.signIn(any(), any()) } returns Result.success(Unit)
        coEvery { authClient.getUserData() } returns Result.success(mockk())

        // When
        signInUseCase(email, password)

        // Then
        coVerify(exactly = 1) {
            authClient.signIn(email, password)
            authClient.getUserData()
        }
    }

    @Test
    fun `SignInUseCase should return success when AuthService#signIn and user email is verified`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authClient.signIn(any(), any()) } returns Result.success(Unit)
        coEvery { authClient.getUserData() } returns Result.success(mockk {
            every { emailVerified } returns true
        })

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

    @Test
    fun `SignInUseCase should return failure with the exception when AuthService#getUserData returns failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("An error occurred")

        coEvery { authClient.signIn(any(), any()) } returns Result.success(Unit)
        coEvery { authClient.getUserData() } returns Result.failure(exception)

        // When
        val result = signInUseCase(email, password)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }

    @Test
    fun `SignInUseCase should return failure when user email is not verified`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"

        coEvery { authClient.signIn(any(), any()) } returns Result.success(Unit)
        coEvery { authClient.getUserData() } returns Result.success(mockk {
            every { emailVerified } returns false
        })
        coEvery { authClient.deleteUser() } returns Result.success(Unit)

        // When
        val result = signInUseCase(email, password)

        // Then
        coVerify { authClient.deleteUser() }
        assert(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }
}