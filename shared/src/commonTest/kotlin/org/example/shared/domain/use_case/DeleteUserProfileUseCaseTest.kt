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
class DeleteUserProfileUseCaseTest {
    private lateinit var deleteUserUseCase: DeleteUserUseCase
    private lateinit var authService: AuthService

    @Before
    fun setUp() {
        authService = mockk(relaxed = true)
        deleteUserUseCase = DeleteUserUseCase(authService)
    }

    @Test
    fun `invoke should call deleteUser from authService`() = runTest {
        // When
        deleteUserUseCase()

        // Then
        coVerify { authService.deleteUser() }
    }

    @Test
    fun `invoke should return result#success when authService#deleteUser is successful`() = runTest {
        // Given
        coEvery { authService.deleteUser() } returns Result.success(Unit)

        // When
        val result = deleteUserUseCase()

        // Then
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return result#failure when authService#deleteUser throws an exception`() = runTest {
        // Given
        val exception = Exception("Test exception")
        coEvery { authService.deleteUser() } returns Result.failure(exception)

        // When
        val result = deleteUserUseCase()

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }
}