package org.example.shared.data.repository.component

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueryByCurriculumIdRepositoryComponentTest {
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

    private lateinit var component: QueryByCurriculumIdRepositoryComponent<TestModel, TestEntity>
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
            QueryByCurriculumIdRepositoryComponent.CURRICULUM_STRATEGY_KEY,
            QueryByCurriculumIdRepositoryComponent.CurriculumQueryStrategy { _ ->
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

        component = QueryByCurriculumIdRepositoryComponent(config)
    }

    @Test
    fun `getByCurriculumId should return models when curriculum exists`() = runTest {
        // Given
        val expected = listOf(testModel)
        every { modelMapper.toModel(any()) } returns testModel
        queryStrategies.withCustomQuery(
            QueryByCurriculumIdRepositoryComponent.CURRICULUM_STRATEGY_KEY,
            QueryByCurriculumIdRepositoryComponent.CurriculumQueryStrategy { curriculumId ->
                assertEquals(TEST_CURRICULUM_ID, curriculumId)
                flowOf(listOf(testEntity))
            }
        )

        // When
        val result = component.getByCurriculumId(TEST_CURRICULUM_ID)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getByCurriculumId should return empty list when curriculum has no associated models`() = runTest {
        // Given
        every { modelMapper.toModel(any()) } returns testModel
        queryStrategies.withCustomQuery(
            QueryByCurriculumIdRepositoryComponent.CURRICULUM_STRATEGY_KEY,
            QueryByCurriculumIdRepositoryComponent.CurriculumQueryStrategy { _ ->
                flowOf(emptyList<TestEntity>())
            }
        )

        // When
        val result = component.getByCurriculumId(TEST_CURRICULUM_ID)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun `getByCurriculumId should propagate errors from query strategy`() = runTest {
        // Given
        val expectedException = RuntimeException("Query strategy error")
        every { modelMapper.toModel(any()) } returns testModel
        queryStrategies.withCustomQuery(
            QueryByCurriculumIdRepositoryComponent.CURRICULUM_STRATEGY_KEY,
            QueryByCurriculumIdRepositoryComponent.CurriculumQueryStrategy { _ ->
                flow<List<TestEntity>> { throw expectedException }
            }
        )

        // When
        val result = component.getByCurriculumId(TEST_CURRICULUM_ID)

        // Then
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    companion object {
        private const val TEST_CURRICULUM_ID = "curriculum123"

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
    }
}