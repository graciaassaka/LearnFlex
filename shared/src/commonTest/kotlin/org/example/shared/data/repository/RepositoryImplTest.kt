package org.example.shared.data.repository

import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.dao.contract.BaseDao
import org.example.shared.data.repository.util.ModelMapper
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.contract.DatabaseRecord
import org.example.shared.domain.repository.Repository
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.sync.SyncOperation
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RepositoryImplTest {
    private lateinit var repository: Repository<TestModel>
    private lateinit var remoteDataSource: RemoteDataSource<TestModel>
    private lateinit var dao: BaseDao<TestModel>
    private lateinit var syncManager: SyncManager<TestModel>

    @Before
    fun setUp() {
        remoteDataSource = mockk()
        dao = mockk()
        syncManager = mockk()

        repository = object : RepositoryImpl<TestModel, TestModel>(
            remoteDataSource = remoteDataSource,
            dao = dao,
            syncManager = syncManager,
            syncOperationFactory = { type, model -> SyncOperation(type, model) },
            modelMapper = TestModelMapper
        ) {}
    }

    @Test
    fun `create should insert the model and queue a create operation`() = runTest {
        // Given
        coEvery { dao.insert(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.create(testModel)

        // Then
        coVerify {
            dao.insert(any())
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `update should update the model and queue an update operation`() = runTest {
        // Given
        coEvery { dao.update(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.update(testModel)

        // Then
        coVerify {
            dao.update(any())
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `get should return the model from the local database`() = runTest {
        // Given
        coEvery { dao.get(any()) } returns testModel
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = repository.get(testModel.id).single()

        // Then
        coVerify {
            dao.get(any())
            syncManager.queueOperation(any())
        }
        assertTrue(result.isSuccess)
        assertEquals(testModel, result.getOrNull())
    }

    @Test
    fun `get should return the model from the remote data source`() = runTest {
        // Given
        val updatedModel = testModel.copy(name = "New name", lastUpdated = 1234567891)
        coEvery { dao.get(any()) } returns null
        coEvery { remoteDataSource.fetch(any()) } returns Result.success(updatedModel)
        coEvery { dao.insert(any()) } just runs

        // When
        val result = repository.get(testModel.id).single()

        // Then
        coVerify {
            dao.get(any())
            remoteDataSource.fetch(any())
            dao.insert(any())
        }
        assertTrue(result.isSuccess)
        assertNotEquals(testModel, result.getOrNull())
    }

    @Test
    fun `get should return an error if the model cannot be retrieved`() = runTest {
        // Given
        val exception = Exception("Test exception")
        coEvery { dao.get(any()) } throws exception

        // When
        val result = repository.get(testModel.id).single()

        // Then
        coVerify { dao.get(any()) }
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `delete should delete the model and queue a delete operation`() = runTest {
        // Given
        coEvery { dao.delete(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.delete(testModel)

        // Then
        coVerify {
            dao.delete(any())
            syncManager.queueOperation(any())
        }
    }

    companion object {
        private val testModel = TestModel(
            id = "test123",
            name = "Test Model",
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )
    }
}

@Serializable
private data class TestModel(
    override val id: String,
    val name: String,
    override val createdAt: Long,
    override val lastUpdated: Long
) : DatabaseRecord

private object TestModelMapper : ModelMapper<TestModel, TestModel> {
    override fun toEntity(model: TestModel) = model
    override fun toModel(entity: TestModel) = entity
}
