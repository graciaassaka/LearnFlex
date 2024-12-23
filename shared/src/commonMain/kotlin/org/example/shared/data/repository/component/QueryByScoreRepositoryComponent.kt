package org.example.shared.data.repository.component

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.ScoreQueryable
import org.example.shared.domain.repository.util.QueryStrategy
import org.example.shared.domain.storage_operations.QueryByScoreOperation

/**
 * Repository component for handling score-based queries.
 *
 * @param Model Type that must satisfy both DatabaseRecord and ScoreQueryable interfaces
 * @param Entity Type that must be a RoomEntity
 * @property config Repository configuration containing all necessary dependencies
 */
class QueryByScoreRepositoryComponent<Model, Entity : RoomEntity>(
    private val config: RepositoryConfig<Model, Entity>
) : QueryByScoreOperation<Model> where Model : DatabaseRecord, Model : ScoreQueryable {

    /**
     * A query strategy for retrieving a set of IDs that have a score greater than or equal to the given minimum score.
     */
    class ScoreQueryStrategy<Entity : RoomEntity>(
        private val strategy: (String, Int) -> Flow<List<Entity>>
    ) : QueryStrategy<List<Entity>> {
        private var parentId: String? = null
        private var minScore: Int? = null

        /**
         * Configures the query with a parent ID and minimum score.
         *
         * @param newParentId The new parent ID to set.
         * @param newMinScore The new minimum score to set.
         */
        fun configure(newParentId: String, newMinScore: Int): ScoreQueryStrategy<Entity> {
            parentId = newParentId
            minScore = newMinScore
            return this
        }

        /**
         * Executes the query with the set parent ID and minimum score.
         *
         * @return A Flow containing the result of the query.
         * @throws IllegalArgumentException if the parent ID or minimum score is not set.
         */
        override fun execute(): Flow<List<Entity>> {
            requireNotNull(parentId) { "Parent ID must be set before executing query" }
            requireNotNull(minScore) { "Min score must be set before executing query" }
            return strategy(parentId!!, minScore!!)
        }
    }

    /**
     * Retrieves a set of IDs that have a score greater than or equal to the given minimum score.
     *
     * @param parentId The ID of the parent entity.
     * @param minScore The minimum score to filter by.
     * @return A Flow containing the result of the query.
     */
    override suspend fun getByMinScore(parentId: String, minScore: Int): Result<List<Model>> = runCatching {
        val scoreStrategy = config.queryStrategies.getCustomStrategy<List<Entity>>(SCORE_STRATEGY_KEY)
                as ScoreQueryStrategy

        scoreStrategy.configure(parentId, minScore)
            .execute()
            .map { entities -> entities.map(config.modelMapper::toModel) }
            .first()
        }

    companion object {
        const val SCORE_STRATEGY_KEY = "score_query_strategy"
    }
}