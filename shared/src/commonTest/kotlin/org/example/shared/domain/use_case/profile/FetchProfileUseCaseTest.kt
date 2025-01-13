package org.example.shared.domain.use_case.profile

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.User
import org.example.shared.domain.repository.ProfileRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.example.shared.domain.use_case.auth.GetUserDataUseCase
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FetchProfileUseCaseTest {
    private lateinit var fetchProfileUseCase: FetchProfileUseCase
    private lateinit var getUserDataUseCase: GetUserDataUseCase
    private lateinit var repository: ProfileRepository
    private lateinit var userIdRequiredMessage: String

    @Before
    fun setUp() {
        getUserDataUseCase = mockk()
        repository = mockk()
        fetchProfileUseCase = FetchProfileUseCase(getUserDataUseCase, repository)
    }

    @Test
    fun `invoke should emit success when getProfile succeeds`() = runTest {
        // Arrange
        val userData = mockk<User>()
        every { userData.localId } returns "test_user_id"
        coEvery { getUserDataUseCase() } returns Result.success(userData)

        // Mock repository to return our expected profile
        every { repository.get(path) } returns flowOf(Result.success(expectedProfile))

        // Act
        val result = fetchProfileUseCase()

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
        val result = fetchProfileUseCase()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    companion object {
        private val expectedProfile = Profile(
            id = "test_user_id",
            username = "sample_username",
            email = "sample_email@example.com",
            photoUrl = "",
            preferences = Profile.LearningPreferences(Field.COMPUTER_SCIENCE, Level.INTERMEDIATE, ""),
            learningStyle = Profile.LearningStyle(),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
        private val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(expectedProfile.id)
            .build()
    }
}