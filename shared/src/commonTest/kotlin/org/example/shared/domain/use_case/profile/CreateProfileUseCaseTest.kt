package org.example.shared.domain.use_case.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.model.Profile
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.use_case.util.CompoundException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateProfileUseCaseTest {
    private lateinit var createProfileUseCase: CreateProfileUseCase
    private lateinit var repository: ProfileRepository
    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        authClient = mockk(relaxed = true)
        createProfileUseCase = CreateProfileUseCase(repository, authClient)
    }

    @Test
    fun `successful profile creation should execute all steps in correct order`() = runTest {
        // Set up our test data with meaningful values
        val path = "profiles/user123"

        // Mock successful responses for all operations
        coEvery { authClient.updateUsername(profile.username) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.success(Unit)
        coEvery { repository.insert(path, profile, any()) } returns Result.success(Unit)

        // Perform the operation
        val result = createProfileUseCase(path, profile)

        // Verify success and correct order of operations
        assertTrue(result.isSuccess)
        coVerifyOrder {
            authClient.updateUsername(profile.username)
            authClient.updatePhotoUrl(profile.photoUrl)
            repository.insert(path, profile, any())
        }
    }

    @Test
    fun `failed username update should trigger rollback with retries`() = runTest {
        // Simulate username update failure
        val updateError = RuntimeException("Username already taken")
        coEvery { authClient.updateUsername(profile.username) } returns Result.failure(updateError)

        // Perform the operation
        val result = createProfileUseCase(PATH, profile)

        // Verify failure and retry attempts
        assertTrue(result.isFailure)
        assertEquals(updateError, result.exceptionOrNull())

        // Verify retry attempts
        coVerify(exactly = CreateProfileUseCase.RETRY_TIMES) { authClient.updateUsername(profile.username) }

        // Verify no other operations were performed
        coVerify(exactly = 0) {
            authClient.updatePhotoUrl(any())
            repository.insert(any(), any(), any())
        }
    }

    @Test
    fun `successful retry should complete profile creation`() = runTest {
        // First attempt fails, second succeeds
        coEvery { authClient.updateUsername(profile.username) } returnsMany listOf(
            Result.failure(RuntimeException("Temporary error")),
            Result.success(Unit)
        )
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.success(Unit)
        coEvery { repository.insert(PATH, profile, any()) } returns Result.success(Unit)

        // Perform the operation
        val result = createProfileUseCase(PATH, profile)

        // Verify eventual success
        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { authClient.updateUsername(profile.username) }
    }

    @Test
    fun `rollback should restore previous state on failure`() = runTest {
        // Username update succeeds but photo URL update fails
        coEvery { authClient.updateUsername(profile.username) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns
                Result.failure(RuntimeException("Photo URL update failed"))

        // Mock successful rollback
        coEvery { authClient.updateUsername("") } returns Result.success(Unit)

        // Perform the operation
        val result = createProfileUseCase(PATH, profile)

        // Verify failure and rollback
        assertTrue(result.isFailure)
        coVerify {
            // Verify original operations
            authClient.updateUsername(profile.username)
            authClient.updatePhotoUrl(profile.photoUrl)

            // Verify rollback operations
            authClient.updateUsername("")
        }
    }

    @Test
    fun `failed rollback should throw CompoundException`() = runTest {
        // Original operation fails
        val originalError = RuntimeException("Operation failed")
        coEvery { authClient.updatePhotoUrl(profile.photoUrl) } returns Result.failure(originalError)

        // Rollback also fails
        val rollbackError = RuntimeException("Rollback failed")
        coEvery { authClient.updateUsername("") } returns Result.failure(rollbackError)

        // Perform the operation
        val result = createProfileUseCase(PATH, profile)

        // Verify CompoundException with both errors
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as CompoundException
        assertEquals(originalError, exception.originalError)
        assertEquals(rollbackError, exception.rollbackError)
    }

    companion object {
        private const val PATH = "profiles/user123"
        private const val TIMESTAMP = 1234567890L

        val profile = Profile(
            id = "user123",
            username = "testUser",
            email = "test@example.com",
            photoUrl = "photo.jpg",
            preferences = mockk(),
            learningStyle = mockk(),
            createdAt = TIMESTAMP,
            lastUpdated = TIMESTAMP
        )
    }
}