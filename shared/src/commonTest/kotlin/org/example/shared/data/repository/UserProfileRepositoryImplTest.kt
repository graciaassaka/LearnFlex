package org.example.shared.data.repository

import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.domain.data_source.UserProfileRemoteDataSource
import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepository
import org.example.shared.domain.sync.SyncManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotEquals

class UserProfileRepositoryImplTest {
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var remoteDataSource: UserProfileRemoteDataSource
    private lateinit var syncManager: SyncManager<UserProfile>

    @Before
    fun setUp() {
        userProfileDao = mockk()
        remoteDataSource = mockk()
        syncManager = mockk()
        userProfileRepository = UserProfileRepositoryImpl(remoteDataSource, userProfileDao, syncManager)
    }

    @Test
    fun `createUserProfile should insert the user profile and queue a create operation`() = runTest {
        // Given
        coEvery { userProfileDao.insert(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        userProfileRepository.createUserProfile(userProfile)

        // Then
        coVerify { userProfileDao.insert(any()) }
        coVerify { syncManager.queueOperation(any()) }
    }

    @Test
    fun `updateUserProfile should insert the user profile and queue an update operation`() = runTest {
        // Given
        coEvery { userProfileDao.insert(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        userProfileRepository.updateUserProfile(userProfile)

        // Then
        coVerify { userProfileDao.insert(any()) }
        coVerify { syncManager.queueOperation(any()) }
    }

    @Test
    fun `getUserProfile should return the user profile from the local database`() = runTest {
        // Given
        coEvery { userProfileDao.getActiveProfile() } returns UserProfileEntity.fromUserProfile(userProfile)
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = userProfileRepository.getUserProfile(userProfile.id).single()

        // Then
        coVerify { userProfileDao.getActiveProfile() }
        coVerify { syncManager.queueOperation(any()) }
        assertTrue(result.isSuccess)
        assertEquals(userProfile.id, result.getOrNull()?.id)
    }

    @Test
    fun `getUserProfile should return the user profile from the remote data source`() = runTest {
        // Given
        val updatedUser = UserProfile(
            id = "testId",
            username = "newTestUsername",
            email = "newtest@example.com",
            photoUrl = "https://example.com/newphoto.jpg",
            preferences = LearningPreferences(
                field = "Math",
                level = "Advanced",
                goal = "Master algebra"
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
        coEvery { userProfileDao.getActiveProfile() } returns null
        coEvery { remoteDataSource.fetchUserProfile(updatedUser.id) } returns Result.success(updatedUser)
        coEvery { userProfileDao.insert(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = userProfileRepository.getUserProfile(updatedUser.id).single()

        // Then
        coVerify { userProfileDao.getActiveProfile() }
        coVerify { remoteDataSource.fetchUserProfile(updatedUser.id) }
        coVerify { userProfileDao.insert(any()) }
        assertTrue(result.isSuccess)
        assertEquals(updatedUser.id, result.getOrNull()?.id)
        assertNotEquals(userProfile.username, result.getOrNull()?.username)
    }

    @Test
    fun `getUserProfile should return an error if the user profile cannot be retrieved`() = runTest {
        // Given
        coEvery { userProfileDao.getActiveProfile() } returns null
        coEvery { remoteDataSource.fetchUserProfile(userProfile.id) } throws Exception("Test exception")

        // When
        val result = userProfileRepository.getUserProfile(userProfile.id).single()

        // Then
        coVerify { userProfileDao.getActiveProfile() }
        coVerify { remoteDataSource.fetchUserProfile(userProfile.id) }
        assertTrue(result.isFailure)
        assertEquals("Test exception", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteUserProfile should delete the user profile and queue a delete operation`() = runTest {
        // Given
        coEvery { userProfileDao.delete(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        userProfileRepository.deleteUserProfile(userProfile)

        // Then
        coVerify { userProfileDao.delete(any()) }
        coVerify { syncManager.queueOperation(any()) }
    }

    companion object {
        private val userProfile = UserProfile(
            id = "testId",
            username = "testUsername",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.jpg",
            preferences = LearningPreferences(
                field = "Science",
                level = "Beginner",
                goal = "Understand the basics"
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}