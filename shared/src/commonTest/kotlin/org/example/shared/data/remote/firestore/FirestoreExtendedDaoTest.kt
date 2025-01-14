package org.example.shared.data.remote.firestore

import dev.gitlive.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.storage_operations.util.PathBuilder
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

    private val testCollectionPath = PathBuilder()
        .collection(Collection.TEST)
        .build()
    private val testTimestamp = 1000L
    private val testModels = listOf(
        TestModel(id = "test1", name = "Test Item 1", lastUpdated = testTimestamp),
        TestModel(id = "test2", name = "Test Item 2", lastUpdated = testTimestamp),
        TestModel(id = "test3", name = "Test Item 3", lastUpdated = testTimestamp)
    )

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        collectionRef = mockk()
        documentRef = mockk()
        batch = mockk(relaxed = true)

        every { firestore.collection(any()) } returns collectionRef
        every { collectionRef.document(any()) } returns documentRef
        every { firestore.batch() } returns batch
        every { firestore.document(any()) } returns documentRef

        dao = FirestoreExtendedDao(
            firestore = firestore,
            serializer = TestModel.serializer()
        )
    }

    @Test
    fun `insertAll should batch insert documents and update parent timestamps`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = dao.insertAll(testModels, testCollectionPath, testTimestamp)

        // Assert
        assertTrue(result.isSuccess)

        // Verify each model was inserted
        testModels.forEach { model ->
            verifyOrder {
                batch.set(any(), TestModel.serializer(), model) { encodeDefaults = true }
                batch.update(any(), mapOf("lastUpdated" to testTimestamp))
            }
        }

        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `updateAll should batch update documents and update parent timestamps`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = dao.updateAll(testModels, testCollectionPath, testTimestamp)

        // Assert
        assertTrue(result.isSuccess)

        testModels.forEach { model ->
            // Verify document update
            verifyOrder {
                batch.update(any(), TestModel.serializer(), model) { encodeDefaults = true }
                batch.update(any(), mapOf("lastUpdated" to testTimestamp))
            }
        }

        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `deleteAll should update timestamps and batch delete documents`() = runTest {
        // Arrange
        coEvery { batch.commit() } returns Unit

        // Act
        val result = dao.deleteAll(testModels, testCollectionPath, testTimestamp)

        // Assert
        assertTrue(result.isSuccess)

        testModels.forEach { _ ->
            verifyOrder {
                batch.update(any(), mapOf("lastUpdated" to testTimestamp))
                batch.delete(any())
            }
        }

        coVerify(exactly = 1) { batch.commit() }
    }

    @Test
    fun `getAllFromSingleCollection should successfully retrieve all documents`() = runTest {
        // Arrange
        val querySnapshot = mockk<QuerySnapshot>()
        val documentSnapshots = testModels.map { model ->
            val docSnapshot = mockk<DocumentSnapshot>()
            every { docSnapshot.data(TestModel.serializer()) } returns model
            docSnapshot
        }
        coEvery { firestore.collection(testCollectionPath.value).get(source = Source.SERVER) } returns querySnapshot
        every { querySnapshot.documents } returns documentSnapshots

        // Act
        val result = dao.getAll(testCollectionPath)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { firestore.collection(testCollectionPath.value).get(source = Source.SERVER) }
    }

    @Test
    fun `getAllFromSingleCollection should return failure when an exception occurs`() = runTest {
        // Arrange
        val querySnapshot = mockk<QuerySnapshot>()
        val documentSnapshots = listOf(mockk<DocumentSnapshot>())
        val exception = Exception("Test exception")

        coEvery { firestore.collection(testCollectionPath.value).get(source = Source.SERVER) } returns querySnapshot
        every { querySnapshot.documents } returns documentSnapshots
        every { documentSnapshots[0].data(TestModel.serializer()) } throws exception

        // Act
        val result = dao.getAll(testCollectionPath)

        // Assert
        assertTrue(result.isFailure)
        coVerify(exactly = 1) { firestore.collection(testCollectionPath.value).get(source = Source.SERVER) }
    }
}