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
import org.example.shared.domain.model.interfaces.ScoreQueryable
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.sync.SyncManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScoreQueryByScoreRepositoryComponentTest {
    @Serializable
    private data class TestModel(
        override val id: String,
        val name: String,
        override val createdAt: Long,
        override val lastUpdated: Long,
        override val quizScore: Int,
        override val quizScoreMax: Int
    ) : DatabaseRecord, ScoreQueryable

    private data class TestEntity(
        override val id: String,
        val name: String,
        override val createdAt: Long,
        override val lastUpdated: Long,
        val quizScore: Int
    ) : RoomEntity

    private lateinit var component: QueryByScoreRepositoryComponent<TestModel, TestEntity>
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
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { _, _ ->
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

        every { modelMapper.toModel(any()) } returns testModel

        component = QueryByScoreRepositoryComponent(config)
    }

    @Test
    fun `getByMinScore should return ids when score meets criteria`() = runTest {
        // Given
        val expected = listOf(testModel)
        queryStrategies.withCustomQuery(
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { parentId, minScore ->
                assertEquals(TEST_PARENT_ID, parentId)
                assertEquals(TEST_MIN_SCORE, minScore)
                flowOf(listOf(testEntity))
            }
        )

        // When
        val result = component.getByMinScore(TEST_PARENT_ID, TEST_MIN_SCORE)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getByMinScore should return empty set when no ids meet criteria`() = runTest {
        // Given
        queryStrategies.withCustomQuery(
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { _, _ ->
                flowOf(emptyList<TestEntity>())
            }
        )

        // When
        val result = component.getByMinScore(TEST_PARENT_ID, TEST_MIN_SCORE)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun `getByMinScore should propagate errors from query strategy`() = runTest {
        // Given
        val expectedException = RuntimeException("Query strategy error")
        queryStrategies.withCustomQuery(
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { _, _ ->
                flow<List<TestEntity>> { throw expectedException }
            }
        )

        // When
        val result = component.getByMinScore(TEST_PARENT_ID, TEST_MIN_SCORE)

        // Then
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    companion object {
        private const val TEST_PARENT_ID = "parent123"
        private const val TEST_MIN_SCORE = 80

        private val testModel = TestModel(
            id = "test123",
            name = "Test Model",
            quizScore = 85,
            quizScoreMax = 100,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )

        private val testEntity = TestEntity(
            id = "test123",
            name = "Test Model",
            quizScore = 85,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )
    }
}