package org.example.shared.data.repository.component

import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.component.QueryByDateRangeRepositoryComponent.Companion.DATE_RANGE_QUERY_STRATEGY_KEY
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.EndTimeQueryable
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueryByDateRangeRepositoryComponentTest {

    @Serializable
    private data class TestModel(
        override val id: String,
        val name: String,
        override val endTime: Long,
        override val createdAt: Long,
        override val lastUpdated: Long
    ) : DatabaseRecord, EndTimeQueryable

    private data class TestEntity(
        override val id: String,
        val name: String,
        val endTime: Long,
        override val createdAt: Long,
        override val lastUpdated: Long
    ) : RoomEntity

    private lateinit var component: QueryByDateRangeRepositoryComponent<TestModel, TestEntity>
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
            DATE_RANGE_QUERY_STRATEGY_KEY,
            QueryByDateRangeRepositoryComponent.DateRangeQueryStrategy { _, _ ->
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

        component = QueryByDateRangeRepositoryComponent(config)
    }

    @Test
    fun `queryByDateRange should return models when dates are within range`() = runTest {
        // Given
        val expectedModels = listOf(testModel)
        val startTime = 1000L
        val endTime = 2000L

        queryStrategies.withCustomQuery(
            DATE_RANGE_QUERY_STRATEGY_KEY,
            QueryByDateRangeRepositoryComponent.DateRangeQueryStrategy { start, end ->
                assertEquals(startTime, start)
                assertEquals(endTime, end)
                flowOf(listOf(testEntity))
            }
        )
        io.mockk.every { modelMapper.toModel(testEntity) } returns testModel

        // When
        val result = component.queryByDateRange(startTime, endTime)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedModels, result.getOrNull())
    }

    @Test
    fun `queryByDateRange should return empty list when no models match date range`() = runTest {
        // Given
        queryStrategies.withCustomQuery(
            DATE_RANGE_QUERY_STRATEGY_KEY,
            QueryByDateRangeRepositoryComponent.DateRangeQueryStrategy<TestEntity> { _, _ ->
                flowOf(emptyList())
            }
        )

        // When
        val result = component.queryByDateRange(1000L, 2000L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun `queryByDateRange should propagate errors from query strategy`() = runTest {
        // Given
        val expectedException = RuntimeException("Query strategy error")
        queryStrategies.withCustomQuery(
            DATE_RANGE_QUERY_STRATEGY_KEY,
            QueryByDateRangeRepositoryComponent.DateRangeQueryStrategy<TestEntity> { _, _ ->
                flow { throw expectedException }
            }
        )

        // When
        val result = component.queryByDateRange(1000L, 2000L)

        // Then
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `queryByDateRange should fail when date range strategy is not configured`() = runTest {
        // Given
        val emptyQueryStrategies = QueryStrategies<TestEntity>()
        val invalidConfig = config.copy(queryStrategies = emptyQueryStrategies)
        component = QueryByDateRangeRepositoryComponent(invalidConfig)

        // When
        val result = component.queryByDateRange(1000L, 2000L)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("strategy '$DATE_RANGE_QUERY_STRATEGY_KEY' not configured") == true)
    }

    companion object {
        private const val TEST_END_TIME = 1500L

        private val testModel = TestModel(
            id = "test123",
            name = "Test Model",
            endTime = TEST_END_TIME,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )

        private val testEntity = TestEntity(
            id = "test123",
            name = "Test Model",
            endTime = TEST_END_TIME,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )
    }
}