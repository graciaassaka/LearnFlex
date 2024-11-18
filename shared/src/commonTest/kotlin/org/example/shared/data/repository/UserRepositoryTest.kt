package org.example.shared.data.repository

import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository
import org.example.shared.domain.sync.SyncManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotEquals

class UserRepositoryTest {
    private lateinit var repository: Repository<UserProfile>
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var remoteDataSource: RemoteDataSource<UserProfile>
    private lateinit var syncManager: SyncManager<UserProfile>

    @Before
    fun setUp() {
        userProfileDao = mockk()
        remoteDataSource = mockk()
        syncManager = mockk()
        repository = UserRepository(remoteDataSource, userProfileDao, syncManager)
    }

    @Test
    fun `createUserProfile should insert the user profile and queue a create operation`() = runTest {
        // Given
        coEvery { userProfileDao.insert(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.create(userProfile)

        // Then
        coVerify { userProfileDao.insert(any()) }
        coVerify { syncManager.queueOperation(any()) }
    }

    @Test
    fun `updateUserProfile should insert the user profile and queue an update operation`() = runTest {
        // Given
        coEvery { userProfileDao.update(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.update(userProfile)

        // Then
        coVerify { userProfileDao.update(any()) }
        coVerify { syncManager.queueOperation(any()) }
    }

    @Test
    fun `getUserProfile should return the user profile from the local database`() = runTest {
        // Given
        coEvery { userProfileDao.get(any()) } returns with(userProfile) {
            UserProfileEntity(id, username, email, photoUrl, preferences, createdAt, lastUpdated)
        }
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = repository.get(userProfile.id).single()

        // Then
        coVerify { userProfileDao.get(any()) }
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
        coEvery { userProfileDao.get(any()) } returns null
        coEvery { remoteDataSource.fetch(updatedUser.id) } returns Result.success(updatedUser)
        coEvery { userProfileDao.insert(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = repository.get(updatedUser.id).single()

        // Then
        coVerify { userProfileDao.get(any()) }
        coVerify { remoteDataSource.fetch(updatedUser.id) }
        coVerify { userProfileDao.insert(any()) }
        assertTrue(result.isSuccess)
        assertEquals(updatedUser.id, result.getOrNull()?.id)
        assertNotEquals(userProfile.username, result.getOrNull()?.username)
    }

    @Test
    fun `getUserProfile should return an error if the user profile cannot be retrieved`() = runTest {
        // Given
        coEvery { userProfileDao.get(any()) } returns null
        coEvery { remoteDataSource.fetch(userProfile.id) } throws Exception("Test exception")

        // When
        val result = repository.get(userProfile.id).single()

        // Then
        coVerify { userProfileDao.get(any()) }
        coVerify { remoteDataSource.fetch(userProfile.id) }
        assertTrue(result.isFailure)
        assertEquals("Test exception", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteUserProfile should delete the user profile and queue a delete operation`() = runTest {
        // Given
        coEvery { userProfileDao.delete(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.delete(userProfile)

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