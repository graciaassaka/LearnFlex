package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.data.model.User
import org.example.shared.domain.service.AuthService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

@ExperimentalCoroutinesApi
actual class GetUserDataUseCaseTest
{
    private lateinit var getUserDataUseCase: GetUserDataUseCase
    private lateinit var authService: AuthService
    private lateinit var user: User

    @Before
    actual fun setUp()
    {
        authService = mockk<AuthService>()
        getUserDataUseCase = GetUserDataUseCase(authService)

        user = User(
            displayName = "John Doe",
            email = "john.doe@example.com",
            photoUrl = "http://example.com/photo.jpg",
            emailVerified = true,
            uid = "123456789"
        )
    }

    @After
    actual fun tearDown() = stopKoin()


    @Test
    actual fun `GetUserDataUseCase should call AuthService#getUserData`() = runTest {
        // Given
        coEvery { authService.getUserData() } returns Result.success(user)

        // When
        getUserDataUseCase()

        // Then
        coVerify(exactly = 1) { authService.getUserData() }
    }

    @Test
    actual fun `GetUserDataUseCase should return success when AuthService#getUserData returns success`() = runTest {
        // Given
        coEvery { authService.getUserData() } returns Result.success(user)

        // When
        val result = getUserDataUseCase()

        // Then
        assert(result.isSuccess)
    }

    @Test
    actual fun `GetUserDataUseCase should return failure with the exception when AuthService#getUserData returns failure`() = runTest {
        // Given
        val exception = Exception("An error occurred")
        coEvery { authService.getUserData() } returns Result.failure(exception)

        // When
        val result = getUserDataUseCase()

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}