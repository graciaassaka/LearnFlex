package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.User
import org.example.shared.domain.service.AuthService
import org.example.shared.domain.service.StorageService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteProfilePictureUseCaseTest {

    private lateinit var deleteProfilePictureUseCase: DeleteProfilePictureUseCase
    private lateinit var storageService: StorageService
    private lateinit var authService: AuthService

    @Before
    fun setUp() {
        storageService = mockk(relaxed = true)
        authService = mockk(relaxed = true)
        deleteProfilePictureUseCase = DeleteProfilePictureUseCase(storageService, authService)
    }

    @Test
    fun `invoke should delete image and update user data successfully`() = runTest {
        // Given
        val user = User(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "old_url",
            emailVerified = true,
            localId = "user123"
        )

        coEvery { authService.getUserData() } returns Result.success(user)
        coEvery { storageService.deleteFile("profile_pictures/${user.localId}.jpg") } returns Result.success(Unit)
        coEvery { authService.updateUserData(user.copy(photoUrl = null)) } returns Result.success(Unit)

        // When
        val result = deleteProfilePictureUseCase.invoke()

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authService.getUserData() }
        coVerify(exactly = 1) { storageService.deleteFile("profile_pictures/${user.localId}.jpg") }
        coVerify(exactly = 1) { authService.updateUserData(user.copy(photoUrl = null)) }
    }

    @Test
    fun `invoke should return failure when getUserData fails`() = runTest {
        // Given
        val exception = Exception("Failed to get user data")

        coEvery { authService.getUserData() } returns Result.failure(exception)

        // When
        val result = deleteProfilePictureUseCase.invoke()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { authService.getUserData() }
        coVerify(exactly = 0) { storageService.deleteFile(any()) }
        coVerify(exactly = 0) { authService.updateUserData(any()) }
    }

    @Test
    fun `invoke should return failure when deleteFile fails`() = runTest {
        // Given
        val user = User(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "old_url",
            emailVerified = true,
            localId = "user123"
        )
        val deleteException = Exception("Delete failed")

        coEvery { authService.getUserData() } returns Result.success(user)
        coEvery { storageService.deleteFile("profile_pictures/${user.localId}.jpg") } returns Result.failure(deleteException)

        // When
        val result = deleteProfilePictureUseCase.invoke()

        // Then
        assertTrue(result.isFailure)
        assertEquals(deleteException, result.exceptionOrNull())
        coVerify(exactly = 1) { authService.getUserData() }
        coVerify(exactly = 1) { storageService.deleteFile("profile_pictures/${user.localId}.jpg") }
        coVerify(exactly = 0) { authService.updateUserData(any()) }
    }

    @Test
    fun `invoke should return failure when updateUserData fails`() = runTest {
        // Given
        val user = User(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "old_url",
            emailVerified = true,
            localId = "user123"
        )
        val updateException = Exception("Update failed")

        coEvery { authService.getUserData() } returns Result.success(user)
        coEvery { storageService.deleteFile("profile_pictures/${user.localId}.jpg") } returns Result.success(Unit)
        coEvery { authService.updateUserData(user.copy(photoUrl = null)) } returns Result.failure(updateException)

        // When
        val result = deleteProfilePictureUseCase.invoke()

        // Then
        assertTrue(result.isFailure)
        assertEquals(updateException, result.exceptionOrNull())
        coVerify(exactly = 1) { authService.getUserData() }
        coVerify(exactly = 1) { storageService.deleteFile("profile_pictures/${user.localId}.jpg") }
        coVerify(exactly = 1) { authService.updateUserData(user.copy(photoUrl = null)) }
    }
}