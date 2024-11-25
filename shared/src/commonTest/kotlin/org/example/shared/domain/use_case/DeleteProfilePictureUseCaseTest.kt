package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.client.StorageClient
import org.example.shared.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteProfilePictureUseCaseTest {

    private lateinit var deleteProfilePictureUseCase: DeleteProfilePictureUseCase
    private lateinit var storageClient: StorageClient
    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        storageClient = mockk(relaxed = true)
        authClient = mockk(relaxed = true)
        deleteProfilePictureUseCase = DeleteProfilePictureUseCase(storageClient, authClient)
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

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { storageClient.deleteFile("profile_pictures/${user.localId}.jpg") } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl("") } returns Result.success(Unit)

        // When
        val result = deleteProfilePictureUseCase.invoke()

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            authClient.getUserData()
            storageClient.deleteFile("profile_pictures/${user.localId}.jpg")
            authClient.updatePhotoUrl("")
        }
    }


    @Test
    fun `invoke should return failure when getUserData fails`() = runTest {
        // Given
        val exception = Exception("Failed to get user data")

        coEvery { authClient.getUserData() } returns Result.failure(exception)

        // When
        val result = deleteProfilePictureUseCase.invoke()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { authClient.getUserData() }
        coVerify(exactly = 0) {
            storageClient.deleteFile(any())
            authClient.updateUsername(any())
        }
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

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { storageClient.deleteFile("profile_pictures/${user.localId}.jpg") } returns Result.failure(
            deleteException
        )

        // When
        val result = deleteProfilePictureUseCase.invoke()

        // Then
        assertTrue(result.isFailure)
        assertEquals(deleteException, result.exceptionOrNull())
        coVerify(exactly = 1) {
            authClient.getUserData()
            storageClient.deleteFile("profile_pictures/${user.localId}.jpg")
        }
        coVerify(exactly = 0) { authClient.updateUsername(any()) }
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

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { storageClient.deleteFile("profile_pictures/${user.localId}.jpg") } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl("") } returns Result.failure(updateException)

        // When
        val result = deleteProfilePictureUseCase.invoke()

        // Then
        assertTrue(result.isFailure)
        assertEquals(updateException, result.exceptionOrNull())
        coVerify(exactly = 1) {
            authClient.getUserData()
            storageClient.deleteFile("profile_pictures/${user.localId}.jpg")
            authClient.updatePhotoUrl("")
        }
    }
}