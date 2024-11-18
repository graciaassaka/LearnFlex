// UserProfileReposImplTest.kt

package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.util.FirestoreCollection
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.Field
import org.example.shared.domain.model.LearningPreferences
import org.example.shared.domain.model.Level
import org.example.shared.domain.model.UserProfile
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserProfileRemoteDataSourceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionRef: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var userProfileRemoteDataSource: RemoteDataSource<UserProfile>

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        collectionRef = mockk<CollectionReference>()
        documentRef = mockk<DocumentReference>()

        every { firestore.collection(FirestoreCollection.USERS.value) } returns collectionRef
        every { collectionRef.path } returns FirestoreCollection.USERS.value
        every { collectionRef.document } returns documentRef
        every { collectionRef.document(userProfile.id) } returns documentRef
        every { collectionRef.parent } returns null

        every { documentRef.id } returns userProfile.id
        every { documentRef.path } returns "${FirestoreCollection.USERS.value}/${userProfile.id}"
        every { documentRef.parent } returns collectionRef

        userProfileRemoteDataSource = UserProfileRemoteDataSource(firestore)
    }

    @Test
    fun `createUserProfile should successfully create a user profile`() = runTest {
        // Arrange
        coEvery { documentRef.set(UserProfile.serializer(), userProfile) { encodeDefaults = true } } returns Unit

        // Act
        val result = userProfileRemoteDataSource.create(userProfile)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.set(userProfile) }
    }

    @Test
    fun `createUserProfile should return a failure when an exception is thrown`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { documentRef.set(UserProfile.serializer(), userProfile) { encodeDefaults = true } } throws exception

        // Act
        val result = userProfileRemoteDataSource.create(userProfile)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() == exception)
        coVerify(exactly = 1) { documentRef.set(userProfile) }
    }

    // UserProfileReposImplTest.kt

    @Test
    fun `getUserProfile should successfully get a user profile`() = runTest {
        // Arrange
        val documentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        coEvery { documentRef.get() } returns documentSnapshot

        // Act
        val result = userProfileRemoteDataSource.fetch(userProfile.id)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.get() }
        coVerify(exactly = 1) { documentSnapshot.data<UserProfile>() }
    }

    @Test
    fun `getUserProfile should return a failure when an exception is thrown`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { documentRef.get() } throws exception

        // Act
        val result = userProfileRemoteDataSource.fetch(userProfile.id)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() == exception)
        coVerify(exactly = 1) { documentRef.get() }
    }

    @Test
    fun `delete should successfully delete a user profile`() = runTest {
        // Arrange
        coEvery { documentRef.delete() } returns Unit

        // Act
        val result = userProfileRemoteDataSource.delete(userProfile.id)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.delete() }
    }

    @Test
    fun `delete should return a failure when an exception is thrown`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { documentRef.delete() } throws exception

        // Act
        val result = userProfileRemoteDataSource.delete(userProfile.id)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() == exception)
        coVerify(exactly = 1) { documentRef.delete() }
    }

    companion object {
        private val userProfile = UserProfile(
            id = "userId123",
            username = "testUser",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.jpg",
            preferences = LearningPreferences(
                field = Field.ComputerScience.name,
                level = Level.Intermediate.name,
                goal = "Learn new things"
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}
