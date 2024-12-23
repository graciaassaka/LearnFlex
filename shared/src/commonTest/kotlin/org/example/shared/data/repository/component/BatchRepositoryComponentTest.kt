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
import org.example.shared.domain.constant.DataCollection
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.dao.ExtendedDao
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
    private lateinit var remoteDao: ExtendedDao<TestModel>
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

        queryStrategies.apply {
            withGetByParent { _ -> flowOf(listOf(testEntity)) }
            byParentStrategy?.setParentId("123")
        }

        config = RepositoryConfig(
            dataCollection = DataCollection.TEST,
            remoteDao = remoteDao,
            localDao = localDao,
            modelMapper = modelMapper,
            syncManager = syncManager,
            queryStrategies = queryStrategies
        )

        component = BatchRepositoryComponent(config)
    }

    @Test
    fun `insertAll success case - should store models with timestamp and queue sync operation`() = runTest {
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        coEvery {
            localDao.insertAll(path = any(), items = any(), timestamp = any())
        } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        val result = component.insertAll(TEST_PATH, listOf(testModel), TIMESTAMP)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            localDao.insertAll(TEST_PATH, any(), TIMESTAMP)
            syncManager.queueOperation(match { it.timestamp == TIMESTAMP })
        }
    }

    @Test
    fun `insertAll failure - localDao throws exception`() = runTest {
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        val exception = RuntimeException("Local storage error")
        coEvery {
            localDao.insertAll(path = any(), items = any(), timestamp = any())
        } throws exception

        val result = component.insertAll(TEST_PATH, listOf(testModel), TIMESTAMP)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 0) { syncManager.queueOperation(any()) }
    }

    @Test
    fun `updateAll success case - should update models with timestamp and queue sync operation`() = runTest {
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        coEvery {
            localDao.updateAll(path = any(), items = any(), timestamp = any())
        } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        val result = component.updateAll(TEST_PATH, listOf(testModel), TIMESTAMP)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            localDao.updateAll(TEST_PATH, any(), TIMESTAMP)
            syncManager.queueOperation(match { it.timestamp == TIMESTAMP })
        }
    }

    @Test
    fun `updateAll failure - localDao throws exception`() = runTest {
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        val exception = RuntimeException("Local update error")
        coEvery {
            localDao.updateAll(path = any(), items = any(), timestamp = any())
        } throws exception

        val result = component.updateAll(TEST_PATH, listOf(testModel), TIMESTAMP)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 0) { syncManager.queueOperation(any()) }
    }

    @Test
    fun `deleteAll success case - should delete models with timestamp and queue sync operation`() = runTest {
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        coEvery {
            localDao.deleteAll(path = any(), items = any(), timestamp = any())
        } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        val result = component.deleteAll(TEST_PATH, listOf(testModel), TIMESTAMP)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            localDao.deleteAll(TEST_PATH, any(), TIMESTAMP)
            syncManager.queueOperation(match { it.timestamp == TIMESTAMP })
        }
    }

    @Test
    fun `deleteAll failure - localDao throws exception`() = runTest {
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        val exception = RuntimeException("Local delete error")
        coEvery {
            localDao.deleteAll(path = any(), items = any(), timestamp = any())
        } throws exception

        val result = component.deleteAll(TEST_PATH, listOf(testModel), TIMESTAMP)

        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { syncManager.queueOperation(any()) }
    }

    @Test
    fun `getAll success case - observe local and remote data`() = runTest {
        every { modelMapper.toModel(any()) } returns testModel
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        every { remoteDao.getAll(any()) } returns flowOf(Result.success(listOf(testModel)))

        val result = component.getAll(TEST_PATH).first()

        assertTrue(result.isSuccess)
        assertEquals(listOf(testModel), result.getOrNull())

        coVerify {
            queryStrategies.byParentStrategy?.execute()
            remoteDao.getAll(TEST_PATH)
            syncManager.queueOperation(any())
        }
    }

    @Test
    fun `getAll failure - remote fetch error`() = runTest {
        val exception = RuntimeException("Remote fetch error")
        every { remoteDao.getAll(any()) } throws exception

        val result = component.getAll(TEST_PATH).first { it.isFailure }

        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun `getAll prevents duplicate emissions`() = runTest {
        every { modelMapper.toModel(any()) } returns testModel
        every { modelMapper.toEntity(any(), any()) } returns testEntity
        every { remoteDao.getAll(any()) } returns flowOf(Result.success(listOf(testModel)))
        coEvery {
            localDao.insertAll(any())
        } just runs

        val results = component.getAll(TEST_PATH)
            .take(2)
            .toList()

        assertEquals(1, results.size)
    }

    @Test
    fun `batch operations fail with non-ExtendedLocalDao`() = runTest {
        val basicLocalDao = mockk<LocalDao<TestEntity>>()
        config = config.copy(localDao = basicLocalDao)
        component = BatchRepositoryComponent(config)

        val result = component.insertAll(TEST_PATH, listOf(testModel), TIMESTAMP)

        assertTrue(result.isFailure)
    }

    @Test
    fun `get operations fail with non-ExtendedRemoteDao`() = runTest {
        val basicRemoteDao = mockk<Dao<TestModel>>()
        config = config.copy(remoteDao = basicRemoteDao)
        component = BatchRepositoryComponent(config)

        val result = component.getAll(TEST_PATH).first { it.isFailure }

        assertTrue(result.isFailure)
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

        private const val TEST_PATH = "profiles/123/items"
        private const val TIMESTAMP = 1234567890L
    }
}