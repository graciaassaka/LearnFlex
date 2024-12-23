package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.storage_operations.CrudOperations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirestoreDaoTest {
    @Serializable
    private data class TestModel(
        override val id: String,
        val name: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val lastUpdated: Long = System.currentTimeMillis()
    ) : DatabaseRecord

    private lateinit var firestore: FirebaseFirestore
    private lateinit var batch: WriteBatch
    private lateinit var collectionRef: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var remoteDataSource: CrudOperations<TestModel>

    private val testCollectionPath = "test/collection"
    private val testTimestamp = 1000L
    private val testModel = TestModel(
        id = "test123",
        name = "Test Item",
        lastUpdated = testTimestamp
    )

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        batch = mockk(relaxed = true)
        collectionRef = mockk()
        documentRef = mockk()

        // Mock the basic Firestore references
        every { firestore.collection(testCollectionPath) } returns collectionRef
        every { collectionRef.document(testModel.id) } returns documentRef
        every { firestore.batch() } returns batch

        // Mock path-related methods for document references
        every { collectionRef.path } returns testCollectionPath
        every { documentRef.path } returns "$testCollectionPath/${testModel.id}"
        every { documentRef.parent } returns collectionRef
        every { documentRef.id } returns testModel.id

        // Mock Firestore document method
        every { firestore.document(any()) } returns documentRef

        remoteDataSource = object : FirestoreBaseDao<TestModel>(
            firestore = firestore,
            serializer = TestModel.serializer()
        ) {}
    }

    @Test
    fun `insert should create document and update parent timestamps in batch`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = remoteDataSource.insert(testCollectionPath, testModel, testTimestamp)

        // Assert
        assertTrue(result.isSuccess)

        // Verify document creation was added to batch
        verify(exactly = 1) {
            batch.set(
                documentRef,
                TestModel.serializer(),
                testModel
            ) { encodeDefaults = true }
        }

        verify {
            batch.update(any(), mapOf("lastUpdated" to testTimestamp))
        }

        // Verify batch was committed
        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `update should modify document and update parent timestamps in batch`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = remoteDataSource.update(testCollectionPath, testModel, testTimestamp)

        // Assert
        assertTrue(result.isSuccess)

        // Verify document update was added to batch
        verify(exactly = 1) {
            batch.update(
                documentRef,
                TestModel.serializer(),
                testModel
            ) { encodeDefaults = true }
        }

        verify {
            batch.update(any(), mapOf("lastUpdated" to testTimestamp))
        }

        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `delete should update timestamps then delete document in batch`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = remoteDataSource.delete(testCollectionPath, testModel, testTimestamp)

        // Assert
        assertTrue(result.isSuccess)

        // Verify order: timestamps first, then deletion
        verifyOrder {
            // First timestamp updates
            batch.update(any(), mapOf("lastUpdated" to testTimestamp))
            // Then document deletion
            batch.delete(documentRef)
        }

        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `batch operations should fail entirely if commit fails`() = runTest {
        // Arrange
        val exception = Exception("Batch commit failed")
        coEvery { batch.commit() } throws exception

        // Act
        val result = remoteDataSource.insert(testCollectionPath, testModel, testTimestamp)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `get should successfully observe document snapshots`() = runTest {
        // Arrange
        val documentSnapshot = mockk<DocumentSnapshot>()
        val snapshotsFlow = flow { emit(documentSnapshot) }

        every { documentSnapshot.exists } returns true
        every { documentRef.snapshots() } returns snapshotsFlow
        coEvery { documentSnapshot.data(TestModel.serializer()) } returns testModel

        // Act
        val result = remoteDataSource.get(testCollectionPath, testModel.id).first()

        // Assert
        assertTrue(result.isSuccess)
        verify(exactly = 1) { documentRef.snapshots() }
    }
}