package org.example.shared.data.repository.component

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.dao.LocalDao
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.DataCollection
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrudRepositoryComponentTest {
    @Serializable
    private data class TestModel(
        override val id: String,
        val name: String,
        override val createdAt: Long,
        override val lastUpdated: Long
    ) : DatabaseRecord

    private data class TestEntity(
        override val id: String,
        val name: String,
        override val createdAt: Long,
        override val lastUpdated: Long
    ) : RoomEntity

    private lateinit var component: CrudRepositoryComponent<TestModel, TestEntity>
    private lateinit var config: RepositoryConfig<TestModel, TestEntity>
    private lateinit var remoteDao: Dao<TestModel>
    private lateinit var localDao: LocalDao<TestEntity>
    private lateinit var modelMapper: ModelMapper<TestModel, TestEntity>
    private lateinit var syncManager: SyncManager<TestModel>
    private lateinit var queryStrategies: QueryStrategies<TestEntity>

    @BeforeTest
    fun setUp() {
        remoteDao = mockk(relaxed = true)
        localDao = mockk(relaxed = true)
        modelMapper = mockk(relaxed = true)
        syncManager = mockk(relaxed = true)
        queryStrategies = QueryStrategies()

        // Configure query strategies
        queryStrategies.withGetById { id ->
            flow {
                emit(
                    if (id == testModel.id) testEntity else null
                )
            }
        }

        config = RepositoryConfig(
            dataCollection = DataCollection.TEST,
            remoteDao = remoteDao,
            localDao = localDao,
            modelMapper = modelMapper,
            syncManager = syncManager,
            queryStrategies = queryStrategies
        )

        component = CrudRepositoryComponent(config)
    }

    @Test
    fun `insert should store model locally with timestamp and queue sync operation`() = runTest {
        // Given
        val timestamp = 1234567890L
        every { modelMapper.toEntity(any()) } returns testEntity
        coEvery { localDao.insert(any(), any(), any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = component.insert(TEST_COLLECTION_PATH, testModel, timestamp)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            // Verify the timestamp is passed through to the local DAO
            localDao.insert(TEST_COLLECTION_PATH, testEntity, timestamp)
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `update should update model locally with timestamp and queue sync operation`() = runTest {
        // Given
        val timestamp = 1234567890L
        every { modelMapper.toEntity(any()) } returns testEntity
        coEvery { localDao.update(any(), any(), any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = component.update(TEST_COLLECTION_PATH, testModel, timestamp)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            // Verify timestamp is passed through
            localDao.update(TEST_COLLECTION_PATH, testEntity, timestamp)
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `get should emit model from local database and queue sync operation`() = runTest {
        // Given
        every { modelMapper.toModel(any()) } returns testModel
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = component.get(TEST_COLLECTION_PATH, testModel.id).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testModel, result.getOrNull())
        coVerify(exactly = 1) {
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `get should fetch from remote and queue sync operation when local returns null`() = runTest {
        // Given
        queryStrategies = QueryStrategies<TestEntity>().apply {
            withGetById { flow { emit(null) } }
        }

        config = config.copy(queryStrategies = queryStrategies)
        component = CrudRepositoryComponent(config)

        every { modelMapper.toEntity(any()) } returns testEntity
        every { modelMapper.toModel(any()) } returns testModel
        every { remoteDao.get(any(), any()) } returns flowOf(Result.success(testModel))

        // When
        val result = component.get(TEST_COLLECTION_PATH, testModel.id).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testModel, result.getOrNull())
        verify(exactly = 1) {
            remoteDao.get(TEST_COLLECTION_PATH, testModel.id)
        }
        coVerify(exactly = 1) {
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `delete should remove model locally with timestamp and queue sync operation`() = runTest {
        // Given
        val timestamp = 1234567890L
        every { modelMapper.toEntity(any()) } returns testEntity
        coEvery { localDao.delete(any(), any(), any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = component.delete(TEST_COLLECTION_PATH, testModel, timestamp)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            // Verify timestamp is passed through
            localDao.delete(TEST_COLLECTION_PATH, testEntity, timestamp)
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `operations should propagate exceptions as failures`() = runTest {
        // Given
        val timestamp = 1234567890L
        val exception = RuntimeException("Test exception")
        coEvery { localDao.insert(any(), any(), any()) } throws exception
        every { modelMapper.toEntity(any()) } returns testEntity

        // When
        val result = component.insert(TEST_COLLECTION_PATH, testModel, timestamp)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private val testModel = TestModel(
            id = "test123",
            name = "Test Model",
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )

        private val testEntity = TestEntity(
            id = "test123",
            name = "Test Model",
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )

        private const val TEST_COLLECTION_PATH = "testCollection"
    }
}