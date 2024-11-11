package org.example.shared.data.repository

import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.data.firestore.LearningStyleReposImpl
import org.example.shared.data.model.StyleBreakdown
import org.example.shared.data.model.StyleResult
import org.example.shared.data.util.FirestoreCollection
import org.example.shared.data.util.Style
import org.example.shared.domain.repository.LearningStyleRepos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LearningStyleImplTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionRef: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var learningStyleRepos: LearningStyleRepos
    private val userId = "userId123"
    private val expectedStyleResult = StyleResult(
            dominantStyle = Style.VISUAL.value,
            styleBreakdown = StyleBreakdown(
                visual = 70,
                reading = 20,
                kinesthetic = 10
            )
        )

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        collectionRef = mockk<CollectionReference>()
        documentRef = mockk<DocumentReference>()

        every { firestore.collection(FirestoreCollection.LEARNING_STYLES.value) } returns collectionRef
        every { collectionRef.path } returns FirestoreCollection.LEARNING_STYLES.value
        every { collectionRef.document } returns documentRef
        every { collectionRef.document(userId) } returns documentRef
        every { collectionRef.parent } returns null

        every { documentRef.id } returns userId
        every { documentRef.path } returns "${FirestoreCollection.LEARNING_STYLES.value}/$userId"
        every { documentRef.parent } returns collectionRef

        learningStyleRepos = LearningStyleReposImpl(firestore)
    }

    @Test
    fun `getLearningStyle should return the user's learning style`() = runTest {
        // Given
        val documentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        coEvery { documentRef.get() } returns documentSnapshot

        // When
        val result = learningStyleRepos.getLearningStyle(userId)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.get() }
    }

    @Test
    fun `getLearningStyle should return a failure when an exception is thrown`() = runTest {
        // Given
        val exception = Exception("Test exception")
        coEvery { documentRef.get() } throws exception

        // When
        val result = learningStyleRepos.getLearningStyle(userId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { documentRef.get() }
    }

    @Test
    fun `setLearningStyle should successfully set the user's learning style`() = runTest {
        // Given
        coEvery { documentRef.set(expectedStyleResult) } returns Unit

        // When
        val result = learningStyleRepos.setLearningStyle(userId, expectedStyleResult)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.set(expectedStyleResult) }
    }

    @Test
    fun `setLearningStyle should return a failure when an exception is thrown`() = runTest {
        // Given
        val exception = Exception("Test exception")
        coEvery { documentRef.set(expectedStyleResult) } throws exception

        // When
        val result = learningStyleRepos.setLearningStyle(userId, expectedStyleResult)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { documentRef.set(expectedStyleResult) }
    }

    @Test
    fun `deleteLearningStyle should successfully delete the user's learning style`() = runTest {
        // Given
        coEvery { documentRef.delete() } returns Unit

        // When
        val result = learningStyleRepos.deleteLearningStyle(userId)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.delete() }
    }

    @Test
    fun `deleteLearningStyle should return a failure when an exception is thrown`() = runTest {
        // Given
        val exception = Exception("Test exception")
        coEvery { documentRef.delete() } throws exception

        // When
        val result = learningStyleRepos.deleteLearningStyle(userId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { documentRef.delete() }
    }
}