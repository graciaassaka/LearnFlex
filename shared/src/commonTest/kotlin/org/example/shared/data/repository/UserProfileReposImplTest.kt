// UserProfileReposImplTest.kt

package org.example.shared.data.repository

import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.data.model.LearningField
import org.example.shared.data.model.LearningLevel
import org.example.shared.data.model.LearningPreferences
import org.example.shared.data.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepos
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UserProfileReposImplTest
{

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionRef: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var userProfileRepos: UserProfileRepos
    private val userProfile = UserProfile(
        id = "userId123",
        username = "testUser",
        email = "test@example.com",
        photoUrl = "https://example.com/photo.jpg",
        preferences = LearningPreferences(
            field = LearningField.ComputerScience.name,
            level = LearningLevel.Intermediate.name,
            goal = "Learn new things"
        )
    )

    @Before
    fun setUp()
    {
        firestore = mockk(relaxed = true)
        collectionRef = mockk<CollectionReference>()
        documentRef = mockk<DocumentReference>()

        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.path } returns "users"
        every { collectionRef.document } returns documentRef
        every { collectionRef.document(userProfile.id) } returns documentRef
        every { collectionRef.parent } returns null

        every { documentRef.id } returns userProfile.id
        every { documentRef.path } returns "users/${userProfile.id}"
        every { documentRef.parent } returns collectionRef

        userProfileRepos = UserProfileReposImpl(firestore)
    }

    @Test
    fun `createUserProfile should successfully create a user profile`() = runTest {
        // Arrange
        coEvery { documentRef.set(UserProfile.serializer(), userProfile) { encodeDefaults = true } } returns Unit

        // Act
        val result = userProfileRepos.createUserProfile(userProfile)

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
        val result = userProfileRepos.createUserProfile(userProfile)

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
        every { documentSnapshot.data<UserProfile>() } returns userProfile
        coEvery { documentRef.get() } returns documentSnapshot

        // Act
        val result = userProfileRepos.getUserProfile(userProfile.id)

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
        val result = userProfileRepos.getUserProfile(userProfile.id)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() == exception)
        coVerify(exactly = 1) { documentRef.get() }
    }

    @Test
    fun `updateUserProfile should successfully update a user profile`() = runTest {
        // Arrange
        coEvery { documentRef.set(userProfile) } returns Unit

        // Act
        val result = userProfileRepos.updateUserProfile(userProfile)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.set(userProfile) }
    }

    @Test
    fun `updateUserProfile should return a failure when an exception is thrown`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { documentRef.set(userProfile) } throws exception

        // Act
        val result = userProfileRepos.updateUserProfile(userProfile)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() == exception)
        coVerify(exactly = 1) { documentRef.set(userProfile) }
    }

    @Test
    fun `deleteUserProfile should successfully delete a user profile`() = runTest {
        // Arrange
        coEvery { documentRef.delete() } returns Unit

        // Act
        val result = userProfileRepos.deleteUserProfile(userProfile.id)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.delete() }
    }

    @Test
    fun `deleteUserProfile should return a failure when an exception is thrown`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { documentRef.delete() } throws exception

        // Act
        val result = userProfileRepos.deleteUserProfile(userProfile.id)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() == exception)
        coVerify(exactly = 1) { documentRef.delete() }
    }
}
