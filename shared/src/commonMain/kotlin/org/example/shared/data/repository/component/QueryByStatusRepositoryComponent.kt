package org.example.shared.data.repository.component

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.example.shared.data.local.entity.interfaces.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.StatusQueryable
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
     * @param Entity The type of the result.
     * @property strategy The function to execute the query.
     */
    class StatusQueryStrategy<Entity : RoomEntity>(
        private val strategy: (String) -> Flow<List<Entity>>
    ) : QueryStrategy<List<Entity>> {
        private var status: String? = null

        /**
         * Configures the strategy with a new status.
         *
         * @param newStatus The new status to set.
         */
        fun configure(newStatus: Status): StatusQueryStrategy<Entity> {
            status = newStatus.value
            return this
        }

        /**
         * Executes the query strategy.
         *
         * @return A flow of the query results.
         * @throws IllegalArgumentException if the status is not set.
         */
        override fun execute(): Flow<List<Entity>> {
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
    override suspend fun getByStatus(status: Status) = runCatching {
        val statusStrategy = config.queryStrategies
            .getCustomStrategy<List<Entity>>(STATUS_STRATEGY_KEY) as StatusQueryStrategy<Entity>

        statusStrategy.configure(status)
            .execute()
            .map { entities -> entities.map(config.modelMapper::toModel) }
            .first()
    }


    companion object {
        const val STATUS_STRATEGY_KEY = "status_query_strategy"
    }
}