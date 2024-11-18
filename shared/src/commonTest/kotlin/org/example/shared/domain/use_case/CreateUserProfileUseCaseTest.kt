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
class CreateUserProfileUseCaseTest {
    private lateinit var createUserProfileUseCase: CreateUserProfileUseCase
    private lateinit var repository: Repository<UserProfile>

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        createUserProfileUseCase = CreateUserProfileUseCase(repository)
    }

    @Test
    fun `invoke should call createUserProfile on userProfileRepos`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)

        // Act
        createUserProfileUseCase(userProfile)

        // Assert
        coVerify { repository.create(userProfile) }
    }

    @Test
    fun `invoke should return success when createUserProfile succeeds`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { repository.create(userProfile) } returns Result.success(Unit)

        // Act
        val result = createUserProfileUseCase(userProfile)

        // Assert
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when createUserProfile fails`() = runTest {
        // Arrange
        val userProfile = mockk<UserProfile>(relaxed = true)
        coEvery { repository.create(userProfile) } returns Result.failure(Exception())

        // Act
        val result = createUserProfileUseCase(userProfile)

        // Assert
        assert(result.isFailure)
    }
}