package org.example.shared.domain.use_case.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.client.StorageClient
import org.example.shared.domain.constant.FileType
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.User
import org.example.shared.domain.use_case.util.CompoundException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UploadProfilePictureUseCaseTest {
    private lateinit var uploadProfilePictureUseCase: UploadProfilePictureUseCase
    private lateinit var storageClient: StorageClient
    private lateinit var authClient: AuthClient
    private lateinit var getProfileUseCase: GetProfileUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase

    @Before
    fun setUp() {
        storageClient = mockk()
        authClient = mockk()
        getProfileUseCase = mockk()
        updateProfileUseCase = mockk()
        uploadProfilePictureUseCase = UploadProfilePictureUseCase(
            storageClient, authClient, getProfileUseCase, updateProfileUseCase
        )
    }

    @Test
    fun `successful upload with no retries needed`() = runTest {
        val user = testUser()
        val profile = testProfile()
        val imageData = byteArrayOf(1, 2, 3)
        val newImageUrl = "new_image_url.jpg"

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { getProfileUseCase(TEST_PATH) } returns Result.success(profile)
        coEvery {
            storageClient.uploadFile(imageData, "profile_pictures/${user.localId}.jpg", FileType.IMAGE)
        } returns Result.success(newImageUrl)
        coEvery { authClient.updatePhotoUrl(newImageUrl) } returns Result.success(Unit)
        coEvery { updateProfileUseCase(TEST_PATH, profile.copy(photoUrl = newImageUrl)) } returns Result.success(Unit)

        val result = uploadProfilePictureUseCase(TEST_PATH, imageData)

        assertTrue(result.isSuccess)
        assertEquals(newImageUrl, result.getOrNull())
        coVerifyOrder {
            storageClient.uploadFile(imageData, "profile_pictures/${user.localId}.jpg", FileType.IMAGE)
            authClient.updatePhotoUrl(newImageUrl)
            updateProfileUseCase(TEST_PATH, profile.copy(photoUrl = newImageUrl))
        }
    }

    @Test
    fun `successful upload after retries`() = runTest {
        val user = testUser()
        val profile = testProfile()
        val imageData = byteArrayOf(1, 2, 3)
        val newImageUrl = "new_image_url.jpg"

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { getProfileUseCase(TEST_PATH) } returns Result.success(profile)
        coEvery {
            storageClient.uploadFile(imageData, "profile_pictures/${user.localId}.jpg", FileType.IMAGE)
        } returnsMany listOf(
            Result.failure(Exception("Temporary error")),
            Result.success(newImageUrl)
        )
        coEvery { authClient.updatePhotoUrl(newImageUrl) } returns Result.success(Unit)
        coEvery { updateProfileUseCase(TEST_PATH, profile.copy(photoUrl = newImageUrl)) } returns Result.success(Unit)

        val result = uploadProfilePictureUseCase(TEST_PATH, imageData)

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) {
            storageClient.uploadFile(imageData, "profile_pictures/${user.localId}.jpg", FileType.IMAGE)
        }
    }

    @Test
    fun `failed upload with successful rollback`() = runTest {
        val user = testUser()
        val profile = testProfile()
        val imageData = byteArrayOf(1, 2, 3)
        val newImageUrl = "new_image_url.jpg"
        val error = RuntimeException("Update failed")

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { getProfileUseCase(TEST_PATH) } returns Result.success(profile)
        coEvery {
            storageClient.uploadFile(imageData, "profile_pictures/${user.localId}.jpg", FileType.IMAGE)
        } returns Result.success(newImageUrl)
        coEvery { authClient.updatePhotoUrl(newImageUrl) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.success(Unit)
        coEvery { storageClient.deleteFile("profile_pictures/${profile.id}.jpg") } returns Result.success(Unit)
        coEvery { updateProfileUseCase(TEST_PATH, profile.copy(photoUrl = newImageUrl)) } returns Result.failure(error)

        val result = uploadProfilePictureUseCase(TEST_PATH, imageData)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify {
            authClient.updatePhotoUrl(profile.photoUrl)
            storageClient.deleteFile("profile_pictures/${profile.id}.jpg")
        }
    }

    @Test
    fun `failed upload with failed rollback throws CompoundException`() = runTest {
        val user = testUser()
        val profile = testProfile()
        val imageData = byteArrayOf(1, 2, 3)
        val newImageUrl = "new_image_url.jpg"
        val originalError = RuntimeException("Update failed")
        val rollbackError = RuntimeException("Rollback failed")

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { getProfileUseCase(TEST_PATH) } returns Result.success(profile)
        coEvery {
            storageClient.uploadFile(imageData, "profile_pictures/${user.localId}.jpg", FileType.IMAGE)
        } returns Result.success(newImageUrl)
        coEvery {
            storageClient.deleteFile("profile_pictures/${profile.id}.jpg")
        } returns Result.failure(rollbackError)
        coEvery { authClient.updatePhotoUrl(newImageUrl) } returns Result.failure(originalError)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.success(Unit)

        val result = uploadProfilePictureUseCase(TEST_PATH, imageData)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as CompoundException
        assertEquals(originalError, exception.originalError)
        assertEquals(rollbackError, exception.rollbackError)
    }

    companion object {
        private const val TEST_PATH = "profiles/user123"
        private fun testUser() = User(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "old_photo.jpg",
            emailVerified = true,
            localId = "user123"
        )

        private fun testProfile() = Profile(
            id = "profile123",
            username = "testuser",
            email = "test@example.com",
            photoUrl = "old_photo.jpg",
            preferences = mockk(),
            learningStyle = mockk(),
            createdAt = 1000L,
            lastUpdated = 1000L
        )
    }
}