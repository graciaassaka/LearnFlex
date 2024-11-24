package org.example.shared.data.sync.handler

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.dao.BaseDao
import org.example.shared.data.repository.util.ModelMapper
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.contract.DatabaseRecord
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation
import org.junit.Before
import org.junit.Test

class SyncHandlerDelegateTest {
    private lateinit var remoteDataSource: RemoteDataSource<TestModel>
    private lateinit var dao: BaseDao<TestModelEntity>
    private lateinit var syncHandler: SyncHandler<TestModel>

    @Before
    fun setup() {
        remoteDataSource = mockk()
        dao = mockk()
        syncHandler = SyncHandlerDelegate(remoteDataSource, dao, TestModelMapper)
    }

    @Test
    fun `handleSync creates model in remote when operation type is CREATE`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperationType.CREATE, testModel)

        coEvery { remoteDataSource.create(testModel) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDataSource.create(testModel) }
    }

    @Test
    fun `handleSync updates model in remote when operation type is UPDATE`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperationType.UPDATE, testModel)

        coEvery { remoteDataSource.create(testModel) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDataSource.create(testModel) }
    }

    @Test
    fun `handleSync deletes model from remote when operation type is DELETE`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperationType.DELETE, testModel)

        coEvery { remoteDataSource.delete(any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDataSource.delete(any()) }
    }

    @Test
    fun `handleSync updates local model when operation type is SYNC and remote model is newer`() = runTest {
        // Given
        val remoteModel = TestModel(
            id = testModel.id,
            createdAt = testModel.createdAt,
            lastUpdated = testModel.lastUpdated + 1
        )
        val operation = SyncOperation(SyncOperationType.SYNC, testModel)

        coEvery { remoteDataSource.fetch(any()) } returns Result.success(remoteModel)
        coEvery { dao.update(any()) } returns Unit

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify(exactly = 1) {
            remoteDataSource.fetch(any())
            dao.update(any())
        }
        coVerify(exactly = 0) {
            remoteDataSource.create(any())
        }
    }

    @Test
    fun `handleSync creates model in remote when operation type is SYNC and local model is newer`() = runTest {
        // Given
        val remoteModel = TestModel(
            id = testModel.id,
            createdAt = testModel.createdAt,
            lastUpdated = testModel.lastUpdated - 1
        )
        val operation = SyncOperation(SyncOperationType.SYNC, testModel)

        coEvery { remoteDataSource.fetch(any()) } returns Result.success(remoteModel)
        coEvery { remoteDataSource.create(any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify(exactly = 1) {
            remoteDataSource.fetch(any())
            remoteDataSource.create(any())
        }
        coVerify(exactly = 0) {
            dao.update(any())
        }
    }

    @Test
    fun `handleSync propagates errors from remote data source`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperationType.CREATE, testModel)
        val exception = Exception("Test exception")

        coEvery { remoteDataSource.create(any()) } returns Result.failure(exception)

        // When
        val result = runCatching { syncHandler.handleSync(operation) }

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private val testModel = TestModel(
            id = "test123",
            createdAt = 1234567890L,
            lastUpdated = 1234567890L
        )
    }
}

@Serializable
private open class TestModel(
    override val id: String,
    override val createdAt: Long,
    override val lastUpdated: Long
) : DatabaseRecord

private data class TestModelEntity(
    override val id: String,
    override val createdAt: Long,
    override val lastUpdated: Long
) : TestModel(id, createdAt, lastUpdated)

private object TestModelMapper : ModelMapper<TestModel, TestModelEntity> {
    override fun toModel(entity: TestModelEntity) = TestModel(entity.id, entity.createdAt, entity.lastUpdated)

    override fun toEntity(model: TestModel) = TestModelEntity(model.id, model.createdAt, model.lastUpdated)
}