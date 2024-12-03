package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirestoreExtendedDaoTest {
    @Serializable
    private data class TestModel(
        override val id: String,
        val name: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val lastUpdated: Long = System.currentTimeMillis()
    ) : DatabaseRecord

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionRef: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var batch: WriteBatch
    private lateinit var dao: FirestoreExtendedDao<TestModel>

    private val testCollectionPath = "test-collection"
    private val testModels = listOf(
        TestModel(id = "test1", name = "Test Item 1"),
        TestModel(id = "test2", name = "Test Item 2"),
        TestModel(id = "test3", name = "Test Item 3")
    )

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        collectionRef = mockk()
        documentRef = mockk()
        batch = mockk(relaxed = true)

        every { firestore.collection(testCollectionPath) } returns collectionRef
        testModels.forEach { model ->
            every { collectionRef.document(model.id) } returns documentRef
        }
        every { firestore.batch() } returns batch

        dao = FirestoreExtendedDao(
            firestore = firestore,
            serializer = TestModel.serializer()
        )
    }

    @Test
    fun `insertAll should successfully insert multiple items`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = dao.insertAll(testCollectionPath, testModels)

        // Assert
        assertTrue(result.isSuccess)
        testModels.forEach { model ->
            verify(exactly = 1) {
                batch.set(
                    documentRef = any<DocumentReference>(),
                    strategy = TestModel.serializer(),
                    data = model
                ) {
                    encodeDefaults = true
                }
            }
        }
        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `insertAll should return failure when batch commit fails`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { batch.commit() } throws exception

        // Act
        val result = dao.insertAll(testCollectionPath, testModels)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `updateAll should successfully update multiple items`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = dao.updateAll(testCollectionPath, testModels)

        // Assert
        assertTrue(result.isSuccess)
        testModels.forEach { model ->
            verify(exactly = 1) {
                batch.update(
                    documentRef = any<DocumentReference>(),
                    strategy = TestModel.serializer(),
                    data = model
                ) {
                    encodeDefaults = true
                }
            }
        }
        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `updateAll should return failure when batch commit fails`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { batch.commit() } throws exception

        // Act
        val result = dao.updateAll(testCollectionPath, testModels)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `deleteAll should successfully delete multiple items`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = dao.deleteAll(testCollectionPath, testModels)

        // Assert
        assertTrue(result.isSuccess)
        verify(exactly = testModels.size) { batch.delete(any()) }
        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `deleteAll should return failure when batch commit fails`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { batch.commit() } throws exception

        // Act
        val result = dao.deleteAll(testCollectionPath, testModels)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getAll should successfully retrieve all documents`() = runTest {
        // Arrange
        val querySnapshot = mockk<QuerySnapshot>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        val snapshotsFlow = flow { emit(querySnapshot) }

        every { collectionRef.snapshots() } returns snapshotsFlow
        every { querySnapshot.documents } returns listOf(documentSnapshot)
        every { documentSnapshot.data(TestModel.serializer()) } returns testModels[0]

        // Act
        val result = dao.getAll(testCollectionPath).first()

        // Assert
        verify(exactly = 1) { collectionRef.snapshots() }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getAll should return failure when an exception occurs`() = runTest {
        // Arrange
        val querySnapshot = mockk<QuerySnapshot>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        val snapshotsFlow = flow { emit(querySnapshot) }
        val exception = Exception("Test exception")

        every { collectionRef.snapshots() } returns snapshotsFlow
        every { querySnapshot.documents } returns listOf(documentSnapshot)
        every { documentSnapshot.data(TestModel.serializer()) } throws exception

        // Act
        val result = dao.getAll(testCollectionPath).first()

        // Assert
        verify(exactly = 1) { collectionRef.snapshots() }
        assertTrue(result.isFailure)
    }
}