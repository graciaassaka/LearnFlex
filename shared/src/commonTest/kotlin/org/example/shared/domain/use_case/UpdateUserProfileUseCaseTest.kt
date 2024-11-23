package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateUserProfileUseCaseTest {
    private lateinit var updateUserProfileUseCase: UpdateUserProfileUseCase
    private lateinit var repository: Repository<UserProfile>

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        updateUserProfileUseCase = UpdateUserProfileUseCase(repository)
    }

    @Test
    fun `invoke should call updateUserProfile on userProfileRepository`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)

        // Act
        updateUserProfileUseCase(userProfile)

        // Assert
        coVerify { repository.update(userProfile) }
    }

    @Test
    fun `invoke should return success when updateUserProfile succeeds`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { repository.update(userProfile) } returns Result.success(Unit)

        // Act
        val result = updateUserProfileUseCase(userProfile)

        // Assert
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when updateUserProfile fails`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { repository.update(userProfile) } returns Result.failure(Exception())

        // Act
        val result = updateUserProfileUseCase(userProfile)

        // Assert
        assert(result.isFailure)
    }
}