package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
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
    private lateinit var collectionRef: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var remoteDataSource: CrudOperations<TestModel>

    private val testCollectionPath = "test-collection"
    private val testModel = TestModel(
        id = "test123",
        name = "Test Item"
    )

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        collectionRef = mockk()
        documentRef = mockk()

        every { firestore.collection(testCollectionPath) } returns collectionRef
        every { collectionRef.document(testModel.id) } returns documentRef
        every { collectionRef.path } returns testCollectionPath
        every { documentRef.path } returns "${testCollectionPath}/${testModel.id}"
        every { documentRef.parent } returns collectionRef
        every { documentRef.id } returns testModel.id

        remoteDataSource = object : FirestoreBaseDao<TestModel>(
            firestore = firestore,
            serializer = TestModel.serializer()
        ) {}
    }

    @Test
    fun `create should successfully create an item`() = runTest {
        // Arrange
        coEvery { documentRef.set(TestModel.serializer(), testModel) { encodeDefaults = true } } returns Unit

        // Act
        val result = remoteDataSource.insert(testCollectionPath, testModel)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.set(TestModel.serializer(), testModel) { encodeDefaults = true } }
    }

    @Test
    fun `create should return failure when exception occurs`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { documentRef.set(TestModel.serializer(), testModel) { encodeDefaults = true } } throws exception

        // Act
        val result = remoteDataSource.insert(testCollectionPath, testModel)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `get should successfully observe document snapshots`() = runTest {
        // Arrange
        val documentSnapshot = mockk<DocumentSnapshot>()
        val snapshotsFlow = flow { emit(documentSnapshot) }

        every { documentRef.snapshots() } returns snapshotsFlow
        coEvery { documentSnapshot.data(TestModel.serializer()) } returns testModel

        // Act
        val result = remoteDataSource.get(testCollectionPath, testModel.id).first()

        // Assert
        assertTrue(result.isSuccess)
        verify(exactly = 1) { documentRef.snapshots() }
    }

    @Test
    fun `get should emit failure when data parsing fails`() = runTest {
        // Arrange
        val documentSnapshot = mockk<DocumentSnapshot>()
        val snapshotsFlow = flow { emit(documentSnapshot) }
        val exception = Exception("Test exception")

        every { documentRef.snapshots() } returns snapshotsFlow
        coEvery { documentSnapshot.data(TestModel.serializer()) } throws exception

        // Act
        val result = remoteDataSource.get(testCollectionPath, testModel.id).first()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { documentRef.snapshots() }
    }

    @Test
    fun `delete should successfully delete an item`() = runTest {
        // Arrange
        coEvery { documentRef.delete() } returns Unit

        // Act
        val result = remoteDataSource.delete(testCollectionPath, testModel)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.delete() }
    }

    @Test
    fun `delete should return failure when exception occurs`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { documentRef.delete() } throws exception

        // Act
        val result = remoteDataSource.delete(testCollectionPath, testModel)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}

