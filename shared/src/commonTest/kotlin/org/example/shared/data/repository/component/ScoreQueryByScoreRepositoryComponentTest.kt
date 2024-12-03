package org.example.shared.data.repository.component

import io.mockk.mockk
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.ScoreQueryable
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
        override val quizScore: Int
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
                flowOf(listOf(testModel.id))
            }
        )

        config = RepositoryConfig(
            remoteDao = mockk(),
            localDao = mockk(),
            modelMapper = modelMapper,
            syncManager = syncManager,
            queryStrategies = queryStrategies
        )

        component = QueryByScoreRepositoryComponent(config)
    }

    @Test
    fun `getIdsByMinScore should return ids when score meets criteria`() = runTest {
        // Given
        val expectedIds = setOf(testModel.id)
        queryStrategies.withCustomQuery(
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { parentId, minScore ->
                assertEquals(TEST_PARENT_ID, parentId)
                assertEquals(TEST_MIN_SCORE, minScore)
                flowOf(expectedIds.toList())
            }
        )

        // When
        val result = component.getIdsByMinScore(TEST_PARENT_ID, TEST_MIN_SCORE).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedIds, result.getOrNull())
    }

    @Test
    fun `getIdsByMinScore should return empty set when no ids meet criteria`() = runTest {
        // Given
        queryStrategies.withCustomQuery(
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { _, _ ->
                flowOf(emptyList())
            }
        )

        // When
        val result = component.getIdsByMinScore(TEST_PARENT_ID, TEST_MIN_SCORE).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptySet(), result.getOrNull())
    }

    @Test
    fun `getIdsByMinScore should propagate errors from query strategy`() = runTest {
        // Given
        val expectedException = RuntimeException("Query strategy error")
        queryStrategies.withCustomQuery(
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { _, _ ->
                flow { throw expectedException }
            }
        )

        // When
        val result = component.getIdsByMinScore(TEST_PARENT_ID, TEST_MIN_SCORE).first()

        // Then
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `getIdsByMinScore should handle multiple emissions`() = runTest {
        // Given
        val firstEmission = listOf(testModel.id)
        val secondEmission = listOf(testModel.id, "id2")

        queryStrategies.withCustomQuery(
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { _, _ ->
                flow {
                    emit(firstEmission)
                    emit(secondEmission)
                }
            }
        )

        // When
        val results = component.getIdsByMinScore(TEST_PARENT_ID, TEST_MIN_SCORE)
            .take(2)
            .toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.isSuccess })
        assertEquals(firstEmission.toSet(), results[0].getOrNull())
        assertEquals(secondEmission.toSet(), results[1].getOrNull())
    }

    @Test
    fun `getIdsByMinScore should deduplicate ids in single emission`() = runTest {
        // Given
        val duplicateIds = listOf(testModel.id, testModel.id, "id2", "id2")
        queryStrategies.withCustomQuery(
            QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
            QueryByScoreRepositoryComponent.ScoreQueryStrategy { _, _ ->
                flowOf(duplicateIds)
            }
        )

        // When
        val result = component.getIdsByMinScore(TEST_PARENT_ID, TEST_MIN_SCORE).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(setOf(testModel.id, "id2"), result.getOrNull())
    }

    companion object {
        private const val TEST_PARENT_ID = "parent123"
        private const val TEST_MIN_SCORE = 80

        private val testModel = TestModel(
            id = "test123",
            name = "Test Model",
            quizScore = 85,
            createdAt = 1234567890,
            lastUpdated = 1234567890
        )
    }
}