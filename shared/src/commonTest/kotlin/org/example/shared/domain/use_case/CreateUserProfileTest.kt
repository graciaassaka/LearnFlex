package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.data.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepos
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateUserProfileTest {
    private lateinit var createUserProfileUseCase: CreateUserProfileUseCase
    private lateinit var userProfileRepos: UserProfileRepos

    @Before
    fun setUp() {
        userProfileRepos = mockk(relaxed = true)
        createUserProfileUseCase = CreateUserProfileUseCase(userProfileRepos)
    }

    @Test
    fun `invoke should call createUserProfile on userProfileRepos`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)

        // Act
        createUserProfileUseCase(userProfile)

        // Assert
        coVerify { userProfileRepos.createUserProfile(userProfile) }
    }

    @Test
    fun `invoke should return success when createUserProfile succeeds`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { userProfileRepos.createUserProfile(userProfile) } returns Result.success(Unit)

        // Act
        val result = createUserProfileUseCase(userProfile)

        // Assert
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when createUserProfile fails`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { userProfileRepos.createUserProfile(userProfile) } returns Result.failure(Exception())

        // Act
        val result = createUserProfileUseCase(userProfile)

        // Assert
        assert(result.isFailure)
    }
}