package org.example.shared.data.sync.handler

import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.dao.LocalDao
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation
import org.junit.Before
import org.junit.Test

class SyncHandlerDelegateTest {
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
    ) : RoomEntity, TestModel(id, createdAt, lastUpdated)

    private object TestModelMapper : ModelMapper<TestModel, TestModelEntity> {
        override fun toModel(entity: TestModelEntity) = TestModel(entity.id, entity.createdAt, entity.lastUpdated)
        override fun toEntity(model: TestModel, parentId: String?) =
            TestModelEntity(model.id, model.createdAt, model.lastUpdated)
    }

    private lateinit var remoteDao: Dao<TestModel>
    private lateinit var localDao: LocalDao<TestModelEntity>
    private lateinit var syncHandler: SyncHandler<TestModel>
    private lateinit var getStrategy: QueryStrategies.SingleEntityStrategyHolder<TestModelEntity>

    @Before
    fun setup() {
        remoteDao = mockk()
        localDao = mockk()
        getStrategy = mockk()
        syncHandler = SyncHandlerDelegate(remoteDao, localDao, getStrategy, TestModelMapper)

        every { getStrategy.setId(any()) } returns getStrategy
        every { getStrategy.execute() } returns flowOf(testEntity)
    }

    @Test
    fun `handleSync creates model in remote when operation type is CREATE`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperation.SyncOperationType.INSERT, TEST_PATH, testModels, TIMESTAMP)

        coEvery { remoteDao.insert(any(), any(), any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDao.insert(any(), any(), any()) }
    }

    @Test
    fun `handleSync updates model in remote when operation type is UPDATE`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperation.SyncOperationType.UPDATE, TEST_PATH, testModels, TIMESTAMP)

        coEvery { remoteDao.update(any(), any(), any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDao.update(any(), any(), any()) }
    }

    @Test
    fun `handleSync deletes model from remote when operation type is DELETE`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperation.SyncOperationType.DELETE, TEST_PATH, testModels, TIMESTAMP)

        coEvery { remoteDao.delete(any(), any(), any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDao.delete(any(), any(), any()) }
    }

    @Test
    fun `handleSync updates local model when operation type is SYNC and remote model is newer`() = runTest {
        // Given
        val remoteModel = TestModel(
            id = testModels.first().id,
            createdAt = testModels.first().createdAt,
            lastUpdated = testModels.first().lastUpdated + 1
        )
        val operation = SyncOperation(SyncOperation.SyncOperationType.SYNC, TEST_PATH, testModels, TIMESTAMP)

        coEvery { remoteDao.get(any(), any()) } returns flowOf(Result.success(remoteModel))
        coEvery { localDao.update(any(), any(), any()) } returns Unit

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify(exactly = 1) {
            remoteDao.get(any(), any())
            localDao.update(any(), any(), any())
        }
        coVerify(exactly = 0) {
            remoteDao.insert(any(), any(), any())
        }
    }

    @Test
    fun `handleSync update model in remote when operation type is SYNC and local model is newer`() = runTest {
        // Given
        val remoteModel = TestModel(
            id = testModels.first().id,
            createdAt = testModels.first().createdAt,
            lastUpdated = testModels.first().lastUpdated - 1
        )
        val operation = SyncOperation(SyncOperation.SyncOperationType.SYNC, TEST_PATH, testModels, TIMESTAMP)

        coEvery { remoteDao.get(any(), any()) } returns flowOf(Result.success(remoteModel))
        coEvery { remoteDao.insert(any(), any(), any()) } returns Result.success(Unit)
        coEvery { remoteDao.update(any(), any(), any()) } returns Result.success(Unit)
        coEvery { localDao.update(any(), any(), any()) } just Runs

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerifyOrder {
            remoteDao.get(any(), any())
            remoteDao.update(any(), any(), any())
        }
    }

    @Test
    fun `handleSync update entity in local when operation type is SYNC and remote model is newer`() = runTest {
        // Given
        val remoteModel = TestModel(
            id = testModels.first().id,
            createdAt = testModels.first().createdAt,
            lastUpdated = testModels.first().lastUpdated + 1
        )
        val operation = SyncOperation(SyncOperation.SyncOperationType.SYNC, TEST_PATH, testModels, TIMESTAMP)

        coEvery { remoteDao.get(any(), any()) } returns flowOf(Result.success(remoteModel))
        coEvery { localDao.update(any(), any(), any()) } returns Unit

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerifyOrder {
            remoteDao.get(any(), any())
            localDao.update(any(), any(), any())
        }
        coVerifyAll(true) {
            remoteDao.insert(any(), any(), any())
            remoteDao.update(any(), any(), any())
        }
    }

    @Test
    fun `handleSync insert model in remote when operation type is SYNC and remote is null`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperation.SyncOperationType.SYNC, TEST_PATH, testModels, TIMESTAMP)

        coEvery { remoteDao.get(any(), any()) } returns flowOf(Result.failure(Exception()))
        coEvery { remoteDao.insert(any(), any(), any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerifyOrder {
            remoteDao.get(any(), any())
            remoteDao.insert(any(), any(), any())
        }
        coVerifyAll(true) {
            localDao.insert(any(), any(), any())
            localDao.update(any(), any(), any())
        }
    }

    @Test
    fun `handleSync insert entity in local when operation type is SYNC and local is null`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperation.SyncOperationType.SYNC, TEST_PATH, testModels, TIMESTAMP)

        coEvery { remoteDao.get(any(), any()) } returns flowOf(Result.success(testModels.first()))
        every { getStrategy.execute() } returns flowOf(null)
        coEvery { localDao.insert(any(), any(), any()) } returns Unit

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerifyOrder {
            remoteDao.get(any(), any())
            localDao.insert(any(), any(), any())
        }
        coVerifyAll(true) {
            remoteDao.insert(any(), any(), any())
            remoteDao.update(any(), any(), any())
        }
    }

    @Test
    fun `handleSync propagates errors from remote data source`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperation.SyncOperationType.INSERT, TEST_PATH, testModels, TIMESTAMP)
        val exception = Exception("Test exception")

        coEvery { remoteDao.insert(any(), any(), any()) } returns Result.failure(exception)

        // When
        val result = runCatching { syncHandler.handleSync(operation) }

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private val testModels = listOf(
            TestModel(
                id = "test123",
                createdAt = 1234567890L,
                lastUpdated = 1234567890L
            )
        )
        private val testEntity = TestModelEntity(
            id = "test123",
            createdAt = 1234567890L,
            lastUpdated = 1234567890L
        )
        private const val TEST_PATH = "test/path"
        private const val TIMESTAMP = 1234567890L
    }
}
