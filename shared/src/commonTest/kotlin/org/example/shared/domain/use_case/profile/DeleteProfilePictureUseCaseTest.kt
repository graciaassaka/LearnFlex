package org.example.shared.domain.use_case.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.client.StorageClient
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.User
import org.example.shared.domain.use_case.util.CompoundException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteProfilePictureUseCaseTest {
    private lateinit var deleteProfilePictureUseCase: DeleteProfilePictureUseCase
    private lateinit var storageClient: StorageClient
    private lateinit var authClient: AuthClient
    private lateinit var fetchProfileUseCase: FetchProfileUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase

    @Before
    fun setUp() {
        storageClient = mockk()
        authClient = mockk()
        fetchProfileUseCase = mockk()
        updateProfileUseCase = mockk()
        deleteProfilePictureUseCase = DeleteProfilePictureUseCase(
            storageClient, authClient, fetchProfileUseCase, updateProfileUseCase
        )
    }

    @Test
    fun `successful deletion with no retries needed`() = runTest {
        val user = testUser()
        val profile = testProfile()

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { authClient.updatePhotoUrl("") } returns Result.success(Unit)
        coEvery { updateProfileUseCase(profile.copy(photoUrl = "")) } returns Result.success(Unit)
        coEvery { storageClient.deleteFile(any()) } returns Result.success(Unit)

        val result = deleteProfilePictureUseCase()

        assertTrue(result.isSuccess)
        coVerifyOrder {
            authClient.updatePhotoUrl("")
            updateProfileUseCase(profile.copy(photoUrl = ""))
            storageClient.deleteFile("profile_pictures/${user.localId}.jpg")
        }
    }

    @Test
    fun `successful deletion after retries`() = runTest {
        val user = testUser()
        val profile = testProfile()

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { authClient.updatePhotoUrl("") } returnsMany listOf(
            Result.failure(Exception("Temporary error")),
            Result.success(Unit)
        )
        coEvery { updateProfileUseCase(profile.copy(photoUrl = "")) } returns Result.success(Unit)
        coEvery { storageClient.deleteFile(any()) } returns Result.success(Unit)

        val result = deleteProfilePictureUseCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { authClient.updatePhotoUrl("") }
    }

    @Test
    fun `failed deletion with successful rollback`() = runTest {
        val user = testUser()
        val profile = testProfile()
        val error = RuntimeException("Update failed")

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { authClient.updatePhotoUrl("") } returns Result.success(Unit)
        coEvery { updateProfileUseCase(profile.copy(photoUrl = "")) } returns Result.success(Unit)
        coEvery { updateProfileUseCase(profile) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.success(Unit)
        coEvery { storageClient.deleteFile(any()) } returns Result.failure(error)

        val result = deleteProfilePictureUseCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify {
            updateProfileUseCase(profile)
            authClient.updatePhotoUrl(profile.photoUrl)
        }
    }

    @Test
    fun `failed deletion with failed rollback throws CompoundException`() = runTest {
        val user = testUser()
        val profile = testProfile()
        val originalError = RuntimeException("Update failed")
        val rollbackError = RuntimeException("Rollback failed")

        coEvery { authClient.getUserData() } returns Result.success(user)
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { authClient.updatePhotoUrl("") } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.failure(rollbackError)
        coEvery { updateProfileUseCase(any()) } returns Result.failure(originalError)

        val result = deleteProfilePictureUseCase()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as CompoundException
        assertEquals(originalError, exception.originalError)
        assertEquals(rollbackError, exception.rollbackError)
    }


    companion object {
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