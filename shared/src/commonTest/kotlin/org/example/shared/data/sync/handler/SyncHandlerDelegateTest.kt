package org.example.shared.data.sync.handler

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.dao.LocalDao
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.dao.RemoteDao
import org.example.shared.domain.model.definition.DatabaseRecord
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
    ) : TestModel(id, createdAt, lastUpdated)

    private object TestModelMapper : ModelMapper<TestModel, TestModelEntity> {
        override fun toModel(entity: TestModelEntity) = TestModel(entity.id, entity.createdAt, entity.lastUpdated)
        override fun toEntity(model: TestModel, parentId: String?) =
            TestModelEntity(model.id, model.createdAt, model.lastUpdated)
    }

    private lateinit var remoteDataSource: RemoteDao<TestModel>
    private lateinit var dao: LocalDao<TestModelEntity>
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
        val operation = SyncOperation(SyncOperationType.INSERT, TEST_PATH, testModels)

        coEvery { remoteDataSource.insert(any(), any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDataSource.insert(any(), any()) }
    }

    @Test
    fun `handleSync updates model in remote when operation type is UPDATE`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperationType.UPDATE, TEST_PATH, testModels)

        coEvery { remoteDataSource.update(any(), any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDataSource.update(any(), any()) }
    }

    @Test
    fun `handleSync deletes model from remote when operation type is DELETE`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperationType.DELETE, TEST_PATH, testModels)

        coEvery { remoteDataSource.delete(any(), any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify { remoteDataSource.delete(any(), any()) }
    }

    @Test
    fun `handleSync updates local model when operation type is SYNC and remote model is newer`() = runTest {
        // Given
        val remoteModel = TestModel(
            id = testModels.first().id,
            createdAt = testModels.first().createdAt,
            lastUpdated = testModels.first().lastUpdated + 1
        )
        val operation = SyncOperation(SyncOperationType.SYNC, TEST_PATH, testModels)

        coEvery { remoteDataSource.get(any(), any()) } returns flowOf(Result.success(remoteModel))
        coEvery { dao.update(any()) } returns Unit

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify(exactly = 1) {
            remoteDataSource.get(any(), any())
            dao.update(any())
        }
        coVerify(exactly = 0) {
            remoteDataSource.insert(any(), any())
        }
    }

    @Test
    fun `handleSync creates model in remote when operation type is SYNC and local model is newer`() = runTest {
        // Given
        val remoteModel = TestModel(
            id = testModels.first().id,
            createdAt = testModels.first().createdAt,
            lastUpdated = testModels.first().lastUpdated - 1
        )
        val operation = SyncOperation(SyncOperationType.SYNC, TEST_PATH, testModels)

        coEvery { remoteDataSource.get(any(), any()) } returns flowOf(Result.success(remoteModel))
        coEvery { remoteDataSource.insert(any(), any()) } returns Result.success(Unit)

        // When
        syncHandler.handleSync(operation)

        // Then
        coVerify(exactly = 1) {
            remoteDataSource.get(any(), any())
            remoteDataSource.insert(any(), any())
        }
        coVerify(exactly = 0) {
            dao.update(any())
        }
    }

    @Test
    fun `handleSync propagates errors from remote data source`() = runTest {
        // Given
        val operation = SyncOperation(SyncOperationType.INSERT, TEST_PATH, testModels)
        val exception = Exception("Test exception")

        coEvery { remoteDataSource.insert(any(), any()) } returns Result.failure(exception)

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
        private const val TEST_PATH = "test/path"
    }
}
