package org.example.shared.data.repository.component

import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.component.QueryByStatusRepositoryComponent.Companion.STATUS_STRATEGY_KEY
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.StatusQueryable
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueryByStatusRepositoryComponentTest {
    @Serializable
    private data class TestModel(
        override val id: String,
        val name: String,
        override val status: String,
        override val createdAt: Long,
        override val lastUpdated: Long
    ) : DatabaseRecord, StatusQueryable

    private data class TestEntity(
        override val id: String,
        val name: String,
        val status: String,
        override val createdAt: Long,
        override val lastUpdated: Long
    ) : RoomEntity

    private lateinit var component: QueryByStatusRepositoryComponent<TestModel, TestEntity>
    private lateinit var config: RepositoryConfig<TestModel, TestEntity>
    private lateinit var modelMapper: ModelMapper<TestModel, TestEntity>
    private lateinit var syncManager: SyncManager<TestModel>
    private lateinit var queryStrategies: QueryStrategies<TestEntity>

    @BeforeTest
    fun setUp() {
        modelMapper = mockk(relaxed = true)
        syncManager = mockk(relaxed = true)
        queryStrategies = QueryStrategies()

        // Default successful query strategy
        queryStrategies.withCustomQuery(
            STATUS_STRATEGY_KEY,
            QueryByStatusRepositoryComponent.StatusQueryStrategy { _ ->
                flowOf(listOf(testEntity))
            }
        )

        config = RepositoryConfig(
            collection = Collection.TEST,
            remoteDao = mockk(),
            localDao = mockk(),
            modelMapper = modelMapper,
            syncManager = syncManager,
            queryStrategies = queryStrategies
        )

        component = QueryByStatusRepositoryComponent(config)
    }

    @Test
    fun `getByStatus should return models when status matches`() = runTest {
        // Given
        val expectedModels = listOf(testModel)
        queryStrategies.withCustomQuery(
            STATUS_STRATEGY_KEY,
            QueryByStatusRepositoryComponent.StatusQueryStrategy { status ->
                assertEquals(Status.FINISHED.value, status)
                flowOf(listOf(testEntity))
            }
        )
        io.mockk.every { modelMapper.toModel(testEntity) } returns testModel

        // When
        val result = component.getByStatus(Status.FINISHED)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedModels, result.getOrNull())
    }

    @Test
    fun `getByStatus should return empty list when no models match status`() = runTest {
        // Given
        queryStrategies.withCustomQuery(
            STATUS_STRATEGY_KEY,
            QueryByStatusRepositoryComponent.StatusQueryStrategy<TestEntity> { _ ->
                flowOf(emptyList())
            }
        )

        // When
        val result = component.getByStatus(Status.UNFINISHED)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun `getByStatus should propagate errors from query strategy`() = runTest {
        // Given
        val expectedException = RuntimeException("Query strategy error")
        queryStrategies.withCustomQuery(
            STATUS_STRATEGY_KEY,
            QueryByStatusRepositoryComponent.StatusQueryStrategy<TestEntity> { _ ->
                flow { throw expectedException }
            }
        )

        // When
        val result = component.getByStatus(Status.FINISHED)

        // Then
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `getByStatus should fail when status strategy is not configured`() = runTest {
        // Given
        val emptyQueryStrategies = QueryStrategies<TestEntity>()
        val invalidConfig = config.copy(queryStrategies = emptyQueryStrategies)
        component = QueryByStatusRepositoryComponent(invalidConfig)

        // When
        val result = component.getByStatus(Status.FINISHED)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("strategy '$STATUS_STRATEGY_KEY' not configured") == true)
    }

    companion object {
        private val testStatus = Status.FINISHED.value

        private val testModel = TestModel(
            id = "test123",
            name = "Test Model",
            status = testStatus,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )

        private val testEntity = TestEntity(
            id = "test123",
            name = "Test Model",
            status = testStatus,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )
    }
}
