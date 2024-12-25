package org.example.shared.domain.use_case.profile

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.User
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.use_case.auth.GetUserDataUseCase
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetProfileUseCaseTest {
    private lateinit var getProfileUseCase: GetProfileUseCase
    private lateinit var getUserDataUseCase: GetUserDataUseCase
    private lateinit var repository: ProfileRepository
    private lateinit var userIdRequiredMessage: String

    @Before
    fun setUp() {
        getUserDataUseCase = mockk()
        repository = mockk()
        getProfileUseCase = GetProfileUseCase(getUserDataUseCase, repository)

        userIdRequiredMessage = GetProfileUseCase.USER_ID_REQUIRED_MESSAGE
    }

    @Test
    fun `invoke should emit success when getProfile succeeds`() = runTest {
        // Arrange
        val userData = mockk<User>()
        every { userData.localId } returns "test_user_id"
        coEvery { getUserDataUseCase() } returns Result.success(userData)

        // Mock repository to return our expected profile
        every { repository.get(PATH, "test_user_id") } returns flowOf(Result.success(expectedProfile))

        // Act
        val result = getProfileUseCase(PATH).first()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedProfile, result.getOrNull())
    }

    @Test
    fun `invoke should emit failure when getUserDataUseCase fails`() = runTest {
        // Arrange
        val exception = Exception("An error occurred")
        coEvery { getUserDataUseCase() } returns Result.failure(exception)

        // Act
        val result = getProfileUseCase(PATH).first()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should emit failure when userId is null`() = runTest {
        // Arrange
        val userData = mockk<User>()
        every { userData.localId } returns null
        coEvery { getUserDataUseCase() } returns Result.success(userData)

        // Act
        val result = getProfileUseCase(PATH).first()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(userIdRequiredMessage, result.exceptionOrNull()?.message)
    }


    companion object {
        private const val PATH = "test/path"
        private val expectedProfile = Profile(
            id = "test_user_id",
            username = "sample_username",
            email = "sample_email@example.com",
            photoUrl = "",
            preferences = Profile.LearningPreferences(),
            learningStyle = Profile.LearningStyle(),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}