package org.example.shared.data.repository.component

import io.mockk.mockk
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.component.QueryByStatusRepositoryComponent.Companion.STATUS_STRATEGY_KEY
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.definition.Status
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.StatusQueryable
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueryByStatusRepositoryComponentTest {

    enum class TestStatus(override val value: String) : Status {
        ACTIVE("active"),
        INACTIVE("inactive")
    }

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
                assertEquals(TestStatus.ACTIVE.value, status)
                flowOf(listOf(testEntity))
            }
        )
        io.mockk.every { modelMapper.toModel(testEntity) } returns testModel

        // When
        val result = component.getByStatus(TestStatus.ACTIVE).first()

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
        val result = component.getByStatus(TestStatus.INACTIVE).first()

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
        val result = component.getByStatus(TestStatus.ACTIVE).first()

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
        val result = component.getByStatus(TestStatus.ACTIVE).first()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("strategy '$STATUS_STRATEGY_KEY' not configured") == true)
    }

    @Test
    fun `getByStatus should handle multiple emissions`() = runTest {
        // Given
        val firstEntity = testEntity
        val secondEntity = testEntity.copy(id = "id2")
        val firstModel = testModel
        val secondModel = testModel.copy(id = "id2")

        queryStrategies.withCustomQuery(
            STATUS_STRATEGY_KEY,
            QueryByStatusRepositoryComponent.StatusQueryStrategy { _ ->
                flow {
                    emit(listOf(firstEntity))
                    emit(listOf(firstEntity, secondEntity))
                }
            }
        )

        io.mockk.every { modelMapper.toModel(firstEntity) } returns firstModel
        io.mockk.every { modelMapper.toModel(secondEntity) } returns secondModel

        // When
        val results = component.getByStatus(TestStatus.ACTIVE)
            .take(2)
            .toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.isSuccess })
        assertEquals(listOf(firstModel), results[0].getOrNull())
        assertEquals(listOf(firstModel, secondModel), results[1].getOrNull())
    }

    companion object {
        private const val TEST_STATUS = "active"

        private val testModel = TestModel(
            id = "test123",
            name = "Test Model",
            status = TEST_STATUS,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )

        private val testEntity = TestEntity(
            id = "test123",
            name = "Test Model",
            status = TEST_STATUS,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )
    }
}
