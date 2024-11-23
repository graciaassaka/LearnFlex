package org.example.shared.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.domain.model.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [28],
    manifest = Config.NONE
)
class DatabaseTest {
    private lateinit var database: LearnFlexDatabase
    private lateinit var userProfileDao: UserProfileDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LearnFlexDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        userProfileDao = database.userProfileDao()
    }

    @After
    fun cleanup() {
        if (::database.isInitialized) {
            database.close()
        }
    }

    @Test
    fun insertAndRetrieveUserProfile() = runTest {
        // Given
        val userProfile = UserProfileEntity(
            id = "test_id",
            username = "test_user",
            email = "test@email.com",
            photoUrl = "test_photo.jpg",
            preferences = LearningPreferences(
                field = Field.Law.name,
                level = Level.Advanced.name,
                goal = "test"
            ),
            learningStyle = StyleResult(
                dominant = "Visual",
                breakdown = StyleBreakdown(
                    visual = 80,
                    reading = 10,
                    kinesthetic = 10
                )
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        // Then
        userProfileDao.insert(userProfile)
        val retrieved = userProfileDao.get(userProfile.id)

        // Assert
        assertNotNull(retrieved)
        assertEquals(retrieved.username, userProfile.username)
    }

    @Test
    fun updateAndRetrieveUserProfile() = runTest {
        // Given
        val originalProfile = UserProfileEntity(
            id = "test_id",
            username = "test_user",
            email = "test@email.com",
            photoUrl = "test_photo.jpg",
            preferences = LearningPreferences(
                field = Field.Law.name,
                level = Level.Advanced.name,
                goal = "test"
            ),
            learningStyle = StyleResult(
                dominant = "Visual",
                breakdown = StyleBreakdown(
                    visual = 80,
                    reading = 10,
                    kinesthetic = 10
                )
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        val updatedProfile = UserProfileEntity(
            id = "test_id",
            username = "updated_user",
            email = "updated_email@email.com",
            photoUrl = "updated_photo.jpg",
            preferences = LearningPreferences(
                field = Field.Math.name,
                level = Level.Beginner.name,
                goal = "updated"
            ),
            learningStyle = StyleResult(
                dominant = "Kinesthetic",
                breakdown = StyleBreakdown(
                    visual = 10,
                    reading = 10,
                    kinesthetic = 80
                )
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        // Then
        userProfileDao.insert(originalProfile)
        userProfileDao.update(updatedProfile)
        val retrieved = userProfileDao.get(originalProfile.id)

        // Assert
        assertNotNull(retrieved)
        assertNotEquals(retrieved.username, originalProfile.username)
    }

    @Test
    fun deleteAndRetrieveUserProfile() = runTest {
        // Given
        val userProfile = UserProfileEntity(
            id = "test_id",
            username = "test_user",
            email = "test@email.com",
            photoUrl = "test_photo.jpg",
            preferences = LearningPreferences(
                field = Field.Law.name,
                level = Level.Advanced.name,
                goal = "test"
            ),
            learningStyle = StyleResult(
                dominant = "Visual",
                breakdown = StyleBreakdown(
                    visual = 80,
                    reading = 10,
                    kinesthetic = 10
                )
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        // Then
        userProfileDao.insert(userProfile)
        userProfileDao.delete(userProfile)
        val retrieved = userProfileDao.get(userProfile.id)

        // Assert
        assertNull(retrieved)
    }
}