package org.example.shared.data.repository.component

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.dao.ExtendedLocalDao
import org.example.shared.data.local.dao.LocalDao
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.dao.ExtendedRemoteDao
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchRepositoryComponentTest {
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

    private lateinit var component: BatchRepositoryComponent<TestModel, TestEntity>
    private lateinit var config: RepositoryConfig<TestModel, TestEntity>
    private lateinit var remoteDao: ExtendedRemoteDao<TestModel>
    private lateinit var localDao: ExtendedLocalDao<TestEntity>
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
        queryStrategies.withGetAll { _ ->
            flowOf(listOf(testEntity))
        }

        config = RepositoryConfig(
            remoteDao = remoteDao,
            localDao = localDao,
            modelMapper = modelMapper,
            syncManager = syncManager,
            queryStrategies = queryStrategies
        )

        component = BatchRepositoryComponent(config)
    }

    @Test
    fun `insertAll should store models locally and queue sync operation`() = runTest {
        // Given
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        coEvery { localDao.insertAll(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = component.insertAll(TEST_COLLECTION_PATH, listOf(testModel))

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            localDao.insertAll(any())
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `insertAll should fail when LocalDao is not ExtendedLocalDao`() = runTest {
        // Given
        val basicLocalDao = mockk<LocalDao<TestEntity>>()
        val invalidConfig = config.copy(localDao = basicLocalDao)
        component = BatchRepositoryComponent(invalidConfig)

        // When
        val result = component.insertAll(TEST_COLLECTION_PATH, listOf(testModel))

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `updateAll should update models locally and queue sync operation`() = runTest {
        // Given
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        coEvery { localDao.updateAll(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = component.updateAll(TEST_COLLECTION_PATH, listOf(testModel))

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            localDao.updateAll(any())
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `deleteAll should remove models locally and queue sync operation`() = runTest {
        // Given
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        coEvery { localDao.deleteAll(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = component.deleteAll(TEST_COLLECTION_PATH, listOf(testModel))

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            localDao.deleteAll(any())
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `getAll should observe local data and fetch remote data`() = runTest {
        // Given
        every { modelMapper.toModel(any()) } returns testModel
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        every { remoteDao.getAll(any()) } returns flowOf(Result.success(listOf(testModel)))
        coEvery { localDao.upsertAll(any()) } just runs

        // When
        val result = component.getAll(TEST_COLLECTION_PATH).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(listOf(testModel), result.getOrNull())
    }

    @Test
    fun `getAll should deduplicate emissions`() = runTest {
        // Given
        every { modelMapper.toModel(any()) } returns testModel
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        every { remoteDao.getAll(any()) } returns flowOf(Result.success(listOf(testModel)))

        // When
        val results = mutableListOf<Result<List<TestModel>>>()
        component.getAll(TEST_COLLECTION_PATH)
            .take(2) // Limit collection to avoid infinite collection
            .toList(results)

        // Then
        assertEquals(1, results.size)
    }

    @Test
    fun `getAll should propagate remote errors`() = runTest {
        // Given
        val exception = RuntimeException("Remote error")
        every { modelMapper.toModel(any()) } returns testModel
        every { remoteDao.getAll(any()) } returns flowOf(Result.failure(exception))

        // When
        val result = component.getAll(TEST_COLLECTION_PATH).first { it.isFailure }

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `operations should extract parent ID from path correctly`() = runTest {
        // Given
        val path = "users/123/items/456"
        every { modelMapper.toEntity(any(), any()) } answers {
            val parentId = secondArg<String?>()
            assertEquals("123", parentId)
            testEntity
        }
        coEvery { localDao.insertAll(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        component.insertAll(path, listOf(testModel))

        // Then
        verify { modelMapper.toEntity(any(), "123") }
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