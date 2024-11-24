package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.util.StorageException
import org.example.shared.domain.constant.FileType
import org.example.shared.domain.model.User
import org.example.shared.domain.service.AuthClient
import org.example.shared.domain.service.StorageClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UploadProfilePictureUseCaseTest {

    private lateinit var uploadProfilePictureUseCase: UploadProfilePictureUseCase
    private lateinit var storageClient: StorageClient
    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        storageClient = mockk(relaxed = true)
        authClient = mockk(relaxed = true)
        uploadProfilePictureUseCase = UploadProfilePictureUseCase(storageClient, authClient)
    }

    @Test
    fun `invoke should upload image and update user data successfully`() = runTest {
        // Given
        val imageData = byteArrayOf(1, 2, 3)
        val user = User(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "old_url",
            emailVerified = true,
            localId = "user123"
        )
        val uploadedUrl = "profile_pictures/user123.jpg"

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery {
            storageClient.uploadFile(
                fileData = imageData,
                path = "profile_pictures/${user.localId}.jpg",
                fileType = FileType.IMAGE
            )
        } returns Result.success(uploadedUrl)
        coEvery { authClient.updatePhotoUrl(uploadedUrl) } returns Result.success(Unit)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            authClient.getUserData()
            storageClient.uploadFile(
                fileData = imageData,
                path = "profile_pictures/${user.localId}.jpg",
                fileType = FileType.IMAGE
            )
            authClient.updatePhotoUrl(uploadedUrl)
        }
    }

    @Test
    fun `invoke should return failure when getUserData fails`() = runTest {
        // Given
        val imageData = byteArrayOf(1, 2, 3)
        val exception = Exception("Failed to get user data")

        coEvery { authClient.getUserData() } returns Result.failure(exception)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { authClient.getUserData() }
        coVerify(exactly = 0) { storageClient.uploadFile(any(), any(), any()) }
        coVerify(exactly = 0) { authClient.updateUsername(any()) }
    }

    @Test
    fun `invoke should return failure when uploadFile fails`() = runTest {
        // Given
        val imageData = byteArrayOf(1, 2, 3)
        val user = User(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "old_url",
            emailVerified = true,
            localId = "user123"
        )
        val uploadException = StorageException.UploadFailure("Upload failed")

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery {
            storageClient.uploadFile(
                fileData = imageData,
                path = "profile_pictures/${user.localId}.jpg",
                fileType = FileType.IMAGE
            )
        } returns Result.failure(uploadException)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isFailure)
        assertEquals(uploadException, result.exceptionOrNull())
        coVerify(exactly = 1) { authClient.getUserData() }
        coVerify(exactly = 1) {
            storageClient.uploadFile(
                fileData = imageData,
                path = "profile_pictures/${user.localId}.jpg",
                fileType = FileType.IMAGE
            )
        }
        coVerify(exactly = 0) { authClient.updateUsername(any()) }
    }

    @Test
    fun `invoke should return failure when updateUserData fails`() = runTest {
        // Given
        val imageData = byteArrayOf(1, 2, 3)
        val user = User(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "old_url",
            emailVerified = true,
            localId = "user123"
        )
        val uploadedUrl = "profile_pictures/user123.jpg"
        val updateException = Exception("Update failed")

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery {
            storageClient.uploadFile(
                fileData = imageData,
                path = "profile_pictures/${user.localId}.jpg",
                fileType = FileType.IMAGE
            )
        } returns Result.success(uploadedUrl)
        coEvery { authClient.updatePhotoUrl(uploadedUrl) } returns Result.failure(updateException)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isFailure)
        assertEquals(updateException, result.exceptionOrNull())
        coVerify(exactly = 1) {
            authClient.getUserData()
            storageClient.uploadFile(
                fileData = imageData,
                path = "profile_pictures/${user.localId}.jpg",
                fileType = FileType.IMAGE
            )
            authClient.updatePhotoUrl(uploadedUrl)
        }
    }

    @Test
    fun `invoke should use correct file path and type`() = runTest {
        // Given
        val imageData = byteArrayOf(1, 2, 3)
        val user = User(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "old_url",
            emailVerified = true,
            localId = "user123"
        )
        val uploadedUrl = "profile_pictures/user123.jpg"

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { storageClient.uploadFile(any(), any(), any()) } returns Result.success(uploadedUrl)
        coEvery { authClient.updateUsername(any()) } returns Result.success(Unit)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            storageClient.uploadFile(
                fileData = imageData,
                path = "profile_pictures/${user.localId}.jpg",
                fileType = FileType.IMAGE
            )
        }
    }
}
