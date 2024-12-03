package org.example.shared.data.repository.component

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.definition.Status
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.StatusQueryable
import org.example.shared.domain.repository.util.QueryStrategy
import org.example.shared.domain.storage_operations.QueryByStatusOperation

/**
 * Repository component for querying models by their status.
 *
 * @param Model The type of the model.
 * @param Entity The type of the entity, which must extend RoomEntity.
 * @property config The repository configuration.
 */
class QueryByStatusRepositoryComponent<Model, Entity : RoomEntity>(
    private val config: RepositoryConfig<Model, Entity>
) : QueryByStatusOperation<Model> where Model : DatabaseRecord, Model : StatusQueryable {

    /**
     * Strategy for querying by status.
     *
     * @param T The type of the result.
     * @property strategy The function to execute the query.
     */
    class StatusQueryStrategy<T>(
        private val strategy: (String) -> Flow<List<T>>
    ) : QueryStrategy<List<T>> {
        private var status: String? = null

        /**
         * Configures the strategy with a new status.
         *
         * @param newStatus The new status to set.
         */
        fun configure(newStatus: Status) {
            status = newStatus.value
        }

        /**
         * Executes the query strategy.
         *
         * @return A flow of the query results.
         * @throws IllegalArgumentException if the status is not set.
         */
        override fun execute(): Flow<List<T>> {
            requireNotNull(status) { "Status must be set before executing query" }
            return strategy(status!!)
        }
    }

    /**
     * Gets models by their status.
     *
     * @param status The status to query by.
     * @return A flow of the query results wrapped in a Result.
     */
    override fun getByStatus(status: Status): Flow<Result<List<Model>>> =
        channelFlow {
            try {
                val statusStrategy = config.queryStrategies
                    .getCustomStrategy<List<Entity>>(STATUS_STRATEGY_KEY) as StatusQueryStrategy<Entity>

                statusStrategy.configure(status)
                statusStrategy.execute()
                    .map { entities -> entities.map(config.modelMapper::toModel) }
                    .collect { models -> send(Result.success(models)) }
            } catch (e: Exception) {
                send(Result.failure(e))
            }
        }

    companion object {
        const val STATUS_STRATEGY_KEY = "status_query_strategy"
    }
}