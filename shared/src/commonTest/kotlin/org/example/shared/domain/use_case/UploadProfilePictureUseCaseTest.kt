package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.data.model.User
import org.example.shared.data.util.FileType
import org.example.shared.data.util.StorageException
import org.example.shared.domain.service.AuthService
import org.example.shared.domain.service.StorageService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UploadProfilePictureUseCaseTest {

    private lateinit var uploadProfilePictureUseCase: UploadProfilePictureUseCase
    private lateinit var storageService: StorageService
    private lateinit var authService: AuthService

    @Before
    fun setUp() {
        storageService = mockk(relaxed = true)
        authService = mockk(relaxed = true)
        uploadProfilePictureUseCase = UploadProfilePictureUseCase(storageService, authService)
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
            uid = "user123"
        )
        val uploadedUrl = "profile_pictures/user123.jpg"

        coEvery { authService.getUserData() } returns Result.success(user)
        coEvery { storageService.uploadFile(
            fileData = imageData,
            path = "profile_pictures/${user.uid}",
            fileType = FileType.IMAGE
        ) } returns Result.success(uploadedUrl)
        coEvery { authService.updateUserData(user.copy(photoUrl = uploadedUrl)) } returns Result.success(Unit)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authService.getUserData() }
        coVerify(exactly = 1) { storageService.uploadFile(
            fileData = imageData,
            path = "profile_pictures/${user.uid}",
            fileType = FileType.IMAGE
        ) }
        coVerify(exactly = 1) { authService.updateUserData(user.copy(photoUrl = uploadedUrl)) }
    }

    @Test
    fun `invoke should return failure when getUserData fails`() = runTest {
        // Given
        val imageData = byteArrayOf(1, 2, 3)
        val exception = Exception("Failed to get user data")

        coEvery { authService.getUserData() } returns Result.failure(exception)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { authService.getUserData() }
        coVerify(exactly = 0) { storageService.uploadFile(any(), any(), any()) }
        coVerify(exactly = 0) { authService.updateUserData(any()) }
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
            uid = "user123"
        )
        val uploadException = StorageException.UploadFailure("Upload failed")

        coEvery { authService.getUserData() } returns Result.success(user)
        coEvery { storageService.uploadFile(
            fileData = imageData,
            path = "profile_pictures/${user.uid}",
            fileType = FileType.IMAGE
        ) } returns Result.failure(uploadException)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isFailure)
        assertEquals(uploadException, result.exceptionOrNull())
        coVerify(exactly = 1) { authService.getUserData() }
        coVerify(exactly = 1) { storageService.uploadFile(
            fileData = imageData,
            path = "profile_pictures/${user.uid}",
            fileType = FileType.IMAGE
        ) }
        coVerify(exactly = 0) { authService.updateUserData(any()) }
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
            uid = "user123"
        )
        val uploadedUrl = "profile_pictures/user123.jpg"
        val updateException = Exception("Update failed")

        coEvery { authService.getUserData() } returns Result.success(user)
        coEvery { storageService.uploadFile(
            fileData = imageData,
            path = "profile_pictures/${user.uid}",
            fileType = FileType.IMAGE
        ) } returns Result.success(uploadedUrl)
        coEvery { authService.updateUserData(user.copy(photoUrl = uploadedUrl)) } returns Result.failure(updateException)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isFailure)
        assertEquals(updateException, result.exceptionOrNull())
        coVerify(exactly = 1) { authService.getUserData() }
        coVerify(exactly = 1) { storageService.uploadFile(
            fileData = imageData,
            path = "profile_pictures/${user.uid}",
            fileType = FileType.IMAGE
        ) }
        coVerify(exactly = 1) { authService.updateUserData(user.copy(photoUrl = uploadedUrl)) }
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
            uid = "user123"
        )
        val uploadedUrl = "profile_pictures/user123.jpg"

        coEvery { authService.getUserData() } returns Result.success(user)
        coEvery { storageService.uploadFile(any(), any(), any()) } returns Result.success(uploadedUrl)
        coEvery { authService.updateUserData(any()) } returns Result.success(Unit)

        // When
        val result = uploadProfilePictureUseCase.invoke(imageData)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            storageService.uploadFile(
                fileData = imageData,
                path = "profile_pictures/${user.uid}",
                fileType = FileType.IMAGE
            )
        }
    }
}
