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
import org.example.shared.domain.constant.Style
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.model.StyleBreakdown
import org.example.shared.domain.model.StyleResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LearningStyleRemoteDataSourceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionRef: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var learningStyleRemoteDataSource: RemoteDataSource<LearningStyle>

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        collectionRef = mockk<CollectionReference>()
        documentRef = mockk<DocumentReference>()

        every { firestore.collection(FirestoreCollection.LEARNING_STYLES.value) } returns collectionRef
        every { collectionRef.path } returns FirestoreCollection.LEARNING_STYLES.value
        every { collectionRef.document } returns documentRef
        every { collectionRef.document(USER_ID) } returns documentRef
        every { collectionRef.parent } returns null

        every { documentRef.id } returns USER_ID
        every { documentRef.path } returns "${FirestoreCollection.LEARNING_STYLES.value}/$USER_ID"
        every { documentRef.parent } returns collectionRef

        learningStyleRemoteDataSource = LearningStyleRemoteDataSource(firestore)
    }

    @Test
    fun `getLearningStyle should return the user's learning style`() = runTest {
        // Given
        val documentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        coEvery { documentRef.get() } returns documentSnapshot

        // When
        val result = learningStyleRemoteDataSource.fetch(USER_ID)

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
        val result = learningStyleRemoteDataSource.fetch(USER_ID)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { documentRef.get() }
    }

    @Test
    fun `create should successfully set the user's learning style`() = runTest {
        // Given
        coEvery { documentRef.set(LearningStyle.serializer(), learningStyle) { encodeDefaults = true } } returns Unit

        // When
        val result = learningStyleRemoteDataSource.create(learningStyle)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.set(learningStyle) }
    }

    @Test
    fun `create should return a failure when an exception is thrown`() = runTest {
        // Given
        val exception = Exception("Test exception")
        coEvery {
            documentRef.set(LearningStyle.serializer(), learningStyle) {
                encodeDefaults = true
            }
        } throws exception

        // When
        val result = learningStyleRemoteDataSource.create(learningStyle)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { documentRef.set(learningStyle) }
    }

    @Test
    fun `delete should successfully delete the user's learning style`() = runTest {
        // Given
        coEvery { documentRef.delete() } returns Unit

        // When
        val result = learningStyleRemoteDataSource.delete(USER_ID)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.delete() }
    }

    @Test
    fun `delete should return a failure when an exception is thrown`() = runTest {
        // Given
        val exception = Exception("Test exception")
        coEvery { documentRef.delete() } throws exception

        // When
        val result = learningStyleRemoteDataSource.delete(USER_ID)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { documentRef.delete() }
    }

    companion object {
        private const val USER_ID = "userId123"
        private val styleResult = StyleResult(
            dominantStyle = Style.VISUAL.value,
            styleBreakdown = StyleBreakdown(
                visual = 70,
                reading = 20,
                kinesthetic = 10
            )
        )
        private val learningStyle = LearningStyle(
            id = USER_ID,
            style = styleResult,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}