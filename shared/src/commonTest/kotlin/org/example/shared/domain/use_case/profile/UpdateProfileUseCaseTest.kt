package org.example.shared.domain.use_case.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Profile
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.example.shared.domain.use_case.util.CompoundException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateProfileUseCaseTest {
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var repository: ProfileRepository
    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        repository = mockk()
        authClient = mockk()
        updateProfileUseCase = UpdateProfileUseCase(repository, authClient)
    }

    @Test
    fun `successful update with no retries needed`() = runTest {
        val profile = testProfile()

        coEvery { authClient.updateUsername(profile.username) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.success(Unit)
        coEvery { repository.update(profile, path, any()) } returns Result.success(Unit)

        val result = updateProfileUseCase(profile)

        assertTrue(result.isSuccess)
        coVerifyOrder {
            authClient.updateUsername(profile.username)
            authClient.updatePhotoUrl(profile.photoUrl)
            repository.update(profile, path, any())
        }
    }

    @Test
    fun `successful update after retries`() = runTest {
        val profile = testProfile()

        coEvery { authClient.updateUsername(profile.username) } returnsMany listOf(
            Result.failure(Exception("Temporary error")),
            Result.success(Unit)
        )
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.success(Unit)
        coEvery { repository.update(profile, path, any()) } returns Result.success(Unit)

        val result = updateProfileUseCase(profile)

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { authClient.updateUsername(profile.username) }
    }

    @Test
    fun `failed update with successful rollback`() = runTest {
        val profile = testProfile()
        val error = RuntimeException("Update failed")

        coEvery { authClient.updateUsername(profile.username) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.failure(error)
        coEvery { authClient.updateUsername("") } returns Result.success(Unit)

        val result = updateProfileUseCase(profile)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify { authClient.updateUsername("") }
    }

    @Test
    fun `failed update with failed rollback throws CompoundException`() = runTest {
        val profile = testProfile()
        val originalError = RuntimeException("Update failed")
        val rollbackError = RuntimeException("Rollback failed")

        coEvery { authClient.updateUsername(profile.username) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.failure(originalError)
        coEvery { authClient.updateUsername("") } returns Result.failure(rollbackError)

        val result = updateProfileUseCase(profile)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as CompoundException
        assertEquals(originalError, exception.originalError)
        assertEquals(rollbackError, exception.rollbackError)
    }

    @Test
    fun `exhausted retries should fail`() = runTest {
        val profile = testProfile()
        val error = RuntimeException("Persistent error")

        coEvery { authClient.updateUsername(profile.username) } returns Result.failure(error)

        val result = updateProfileUseCase(profile)

        assertTrue(result.isFailure)
        coVerify(exactly = UpdateProfileUseCase.RETRY_TIMES) { authClient.updateUsername(profile.username) }
    }

    companion object {
        private fun testProfile() = Profile(
            id = "profile123",
            username = "testuser",
            email = "test@example.com",
            photoUrl = "photo.jpg",
            preferences = mockk(),
            learningStyle = mockk(),
            createdAt = 1000L,
            lastUpdated = 1000L
        )
        private val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(testProfile().id)
            .build()
    }
}