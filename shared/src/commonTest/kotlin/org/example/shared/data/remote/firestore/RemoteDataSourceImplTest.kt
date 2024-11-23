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
import kotlinx.serialization.Serializable
import org.example.shared.data.remote.util.FirestoreCollection
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.contract.DatabaseRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteDataSourceImplTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionRef: CollectionReference
    private lateinit var documentRef: DocumentReference
    private lateinit var remoteDataSource: RemoteDataSource<TestModel>

    private val testCollection = FirestoreCollection.USERS
    private val testModel = TestModel(
        id = "test123",
        name = "Test Item"
    )

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        collectionRef = mockk()
        documentRef = mockk()

        every { firestore.collection(testCollection.value) } returns collectionRef
        every { collectionRef.document(testModel.id) } returns documentRef
        every { collectionRef.path } returns testCollection.value
        every { documentRef.path } returns "${testCollection.value}/${testModel.id}"
        every { documentRef.parent } returns collectionRef
        every { documentRef.id } returns testModel.id

        remoteDataSource = object : RemoteDataSourceImpl<TestModel>(
            firestore = firestore,
            collection = testCollection,
            serializer = TestModel.serializer()
        ) {}
    }

    @Test
    fun `create should successfully create an item`() = runTest {
        // Arrange
        coEvery { documentRef.set(TestModel.serializer(), testModel) { encodeDefaults = true } } returns Unit

        // Act
        val result = remoteDataSource.create(testModel)

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
        val result = remoteDataSource.create(testModel)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `fetch should successfully retrieve an item`() = runTest {
        // Arrange
        val documentSnapshot = mockk<DocumentSnapshot>()
        coEvery { documentRef.get() } returns documentSnapshot
        coEvery { documentSnapshot.data(TestModel.serializer()) } returns testModel

        // Act
        val result = remoteDataSource.fetch(testModel.id)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { documentRef.get() }
    }

    @Test
    fun `fetch should return failure when exception occurs`() = runTest {
        // Arrange
        val exception = Exception("Test exception")
        coEvery { documentRef.get() } throws exception

        // Act
        val result = remoteDataSource.fetch(testModel.id)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `delete should successfully delete an item`() = runTest {
        // Arrange
        coEvery { documentRef.delete() } returns Unit

        // Act
        val result = remoteDataSource.delete(testModel.id)

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
        val result = remoteDataSource.delete(testModel.id)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}

@Serializable
private data class TestModel(
    override val id: String,
    val name: String,
    override val createdAt: Long = System.currentTimeMillis(),
    override val lastUpdated: Long = System.currentTimeMillis()
) : DatabaseRecord