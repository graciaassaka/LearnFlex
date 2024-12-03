package org.example.shared.data.repository.component

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
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
    class ScoreQueryStrategy(
        private val strategy: (String, Int) -> Flow<List<String>>
    ) : QueryStrategy<List<String>> {
        private var parentId: String? = null
        private var minScore: Int? = null

        /**
         * Configures the query with a parent ID and minimum score.
         *
         * @param newParentId The new parent ID to set.
         * @param newMinScore The new minimum score to set.
         */
        fun configure(newParentId: String, newMinScore: Int) {
            parentId = newParentId
            minScore = newMinScore
        }

        /**
         * Executes the query with the set parent ID and minimum score.
         *
         * @return A Flow containing the result of the query.
         * @throws IllegalArgumentException if the parent ID or minimum score is not set.
         */
        override fun execute(): Flow<List<String>> {
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
    override fun getIdsByMinScore(parentId: String, minScore: Int): Flow<Result<Set<String>>> =
        channelFlow {
            try {
                val scoreStrategy = config.queryStrategies.getCustomStrategy<List<String>>(SCORE_STRATEGY_KEY)
                        as ScoreQueryStrategy

                scoreStrategy.configure(parentId, minScore)
                scoreStrategy.execute()
                    .map { ids -> Result.success(ids.toSet()) }
                    .collect { result -> send(result) }
            } catch (e: Exception) {
                send(Result.failure(e))
            }
        }

    companion object {
        const val SCORE_STRATEGY_KEY = "score_query_strategy"
    }
}