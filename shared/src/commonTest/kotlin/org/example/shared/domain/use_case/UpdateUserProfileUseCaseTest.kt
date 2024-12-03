package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.AuthClient
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepository
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateUserProfileUseCaseTest {
    private lateinit var updateUserProfileUseCase: UpdateUserProfileUseCase
    private lateinit var repository: UserProfileRepository
    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        authClient = mockk(relaxed = true)
        updateUserProfileUseCase = UpdateUserProfileUseCase(repository, authClient)
    }

    @Test
    fun `invoke should return success when updateUserProfile succeeds`() = runTest {
        // Arrange
        val path = "test/path"
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { repository.update(path, userProfile) } returns Result.success(Unit)
        coEvery { authClient.updateUsername(userProfile.username) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(userProfile.photoUrl) } returns Result.success(Unit)

        // Act
        val result = updateUserProfileUseCase(path, userProfile)

        // Assert
        coVerify(exactly = 1) {
            repository.update(path, userProfile)
            authClient.updateUsername(userProfile.username)
            authClient.updatePhotoUrl(userProfile.photoUrl)
        }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when updateUserProfile fails`() = runTest {
        // Arrange
        val path = "test/path"
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { repository.update(path, userProfile) } returns Result.failure(Exception())
        coEvery { authClient.updateUsername(userProfile.username) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(userProfile.photoUrl) } returns Result.success(Unit)

        // Act
        val result = updateUserProfileUseCase(path, userProfile)

        // Assert
        coVerify(exactly = 1) { repository.update(path, userProfile) }
        coVerify(exactly = 0) {
            authClient.updateUsername(userProfile.username)
            authClient.updatePhotoUrl(userProfile.photoUrl)
        }
        assert(result.isFailure)
    }

    @Test
    fun `invoke should return failure when updateUsername fails`() = runTest {
        // Arrange
        val path = "test/path"
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { repository.update(path, userProfile) } returns Result.success(Unit)
        coEvery { authClient.updateUsername(userProfile.username) } returns Result.failure(Exception())
        coEvery { authClient.updatePhotoUrl(userProfile.photoUrl) } returns Result.success(Unit)

        // Act
        val result = updateUserProfileUseCase(path, userProfile)

        // Assert
        coVerify(exactly = 1) {
            repository.update(path, userProfile)
            authClient.updateUsername(userProfile.username)
        }
        coVerify(exactly = 0) {
            authClient.updatePhotoUrl(userProfile.photoUrl)
        }
        assert(result.isFailure)
    }

    @Test
    fun `invoke should return failure when updatePhotoUrl fails`() = runTest {
        // Arrange
        val path = "test/path"
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { repository.update(path, userProfile) } returns Result.success(Unit)
        coEvery { authClient.updateUsername(userProfile.username) } returns Result.success(Unit)
        coEvery { authClient.updatePhotoUrl(userProfile.photoUrl) } returns Result.failure(Exception())

        // Act
        val result = updateUserProfileUseCase(path, userProfile)

        // Assert
        coVerify(exactly = 1) {
            repository.update(path, userProfile)
            authClient.updateUsername(userProfile.username)
            authClient.updatePhotoUrl(userProfile.photoUrl)
        }
        assert(result.isFailure)
    }
}