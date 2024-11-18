package org.example.shared.data.sync.handler

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.sync.SyncOperation
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserProfileSyncHandlerTest {
    private lateinit var mockRemoteDataSource: RemoteDataSource<UserProfile>
    private lateinit var mockUserProfileDao: UserProfileDao
    private lateinit var syncHandler: UserProfileSyncHandler

    @BeforeTest
    fun setup() {
        mockRemoteDataSource = mockk()
        mockUserProfileDao = mockk()
        syncHandler = UserProfileSyncHandler(mockRemoteDataSource, mockUserProfileDao)
    }

    @Test
    fun `handleSync creates profile in remote when operation type is CREATE`() = runTest {
        // Given
        val profile = createTestProfile()
        val operation = SyncOperation(SyncOperationType.CREATE, profile)

        coEvery { mockRemoteDataSource.create(profile) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { mockRemoteDataSource.create(profile) }
    }

    @Test
    fun `handleSync updates profile in remote when operation type is UPDATE`() = runTest {
        // Given
        val profile = createTestProfile()
        val operation = SyncOperation(SyncOperationType.UPDATE, profile)

        coEvery { mockRemoteDataSource.create(profile) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { mockRemoteDataSource.create(profile) }
    }

    @Test
    fun `handleSync deletes profile from remote when operation type is DELETE`() = runTest {
        // Given
        val profile = createTestProfile()
        val operation = SyncOperation(SyncOperationType.DELETE, profile)

        coEvery { mockRemoteDataSource.delete(profile.id) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { mockRemoteDataSource.delete(profile.id) }
    }

    @Test
    fun `handleSync updates local profile when remote is newer during SYNC`() = runTest {
        // Given
        val localProfile = createTestProfile(lastUpdated = 100L)
        val remoteProfile = createTestProfile(lastUpdated = 200L)
        val operation = SyncOperation(SyncOperationType.SYNC, localProfile)

        coEvery { mockRemoteDataSource.fetch(localProfile.id) } returns Result.success(remoteProfile)
        coEvery { mockUserProfileDao.update(any()) } just runs
        coEvery { mockRemoteDataSource.create(localProfile) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { mockRemoteDataSource.fetch(localProfile.id) }
        coVerify { mockUserProfileDao.update(any()) }
        coVerify(exactly = 0) { mockRemoteDataSource.create(localProfile) }
    }

    @Test
    fun `handleSync updates remote profile when local is newer during SYNC`() = runTest {
        // Given
        val localProfile = createTestProfile(lastUpdated = 200L)
        val remoteProfile = createTestProfile(lastUpdated = 100L)
        val operation = SyncOperation(SyncOperationType.SYNC, localProfile)

        coEvery { mockRemoteDataSource.fetch(localProfile.id) } returns Result.success(remoteProfile)
        coEvery { mockUserProfileDao.update(any()) } just runs
        coEvery { mockRemoteDataSource.create(localProfile) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { mockRemoteDataSource.fetch(localProfile.id) }
        coVerify(exactly = 0) { mockUserProfileDao.update(any()) }
        coVerify { mockRemoteDataSource.create(localProfile) }
    }

    @Test
    fun `handleSync propagates errors from remote data source`() = runTest {
        // Given
        val profile = createTestProfile()
        val operation = SyncOperation(SyncOperationType.CREATE, profile)
        val exception = Exception("Test exception")

        coEvery { mockRemoteDataSource.create(profile) } returns Result.failure(exception)

        // When
        val result = runCatching { syncHandler.handleSync(operation) }

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}

private fun createTestProfile(lastUpdated: Long = System.currentTimeMillis()) = UserProfile(
    id = "test-id",
    email = "test@example.com",
    username = "testuser",
    photoUrl = "https://example.com/photo.jpg",
    preferences = LearningPreferences(
        field = "Computer Science",
        level = "Beginner",
        goal = "Learn Programming"
    ),
    createdAt = 100L,
    lastUpdated = lastUpdated
)
