package org.example.shared.data.repository.component

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.domain.model.definition.DatabaseRecord
import org.example.shared.domain.model.definition.EndTimeQueryable
import org.example.shared.domain.repository.util.QueryStrategy
import org.example.shared.domain.storage_operations.QueryByDateRangeOperation

/**
 * A repository component for querying data by a date range.
 *
 * @param Model The type of the model.
 * @param Entity The type of the entity, which must extend RoomEntity.
 * @property config The repository configuration.
 */
class QueryByDateRangeRepositoryComponent<Model, Entity : RoomEntity>(
    private val config: RepositoryConfig<Model, Entity>
) : QueryByDateRangeOperation<Model> where Model : DatabaseRecord, Model : EndTimeQueryable {

    /**
     * A strategy for querying data within a date range.
     *
     * @param T The type of the data to be queried.
     * @property strategy The function that performs the query.
     */
    class DateRangeQueryStrategy<T>(
        private val strategy: (Long, Long) -> Flow<List<T>>
    ) : QueryStrategy<List<T>> {
        private var start: Long? = null
        private var end: Long? = null

        /**
         * Configures the start and end times for the query.
         *
         * @param newStart The start time in milliseconds.
         * @param newEnd The end time in milliseconds.
         */
        fun configure(newStart: Long, newEnd: Long) {
            start = newStart
            end = newEnd
        }

        /**
         * Executes the query with the configured start and end times.
         *
         * @return A Flow emitting the list of queried data.
         * @throws IllegalArgumentException if start or end times are not set.
         */
        override fun execute(): Flow<List<T>> {
            requireNotNull(start) { "Start time must be set before executing query" }
            requireNotNull(end) { "End time must be set before executing query" }
            return strategy(start!!, end!!)
        }
    }

    /**
     * Queries data by a date range.
     *
     * @param start The start time in milliseconds.
     * @param end The end time in milliseconds.
     * @return A Flow emitting the result of the query.
     */
    override fun queryByDateRange(start: Long, end: Long) = channelFlow {
        try {
            val strategy = config.queryStrategies.getCustomStrategy<List<Entity>>(DATE_RANGE_QUERY_STRATEGY_KEY)
                    as DateRangeQueryStrategy<Entity>

            strategy.configure(start, end)
            strategy.execute()
                .map { entities -> entities.map(config.modelMapper::toModel) }
                .collect { models -> send(Result.success(models)) }
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    companion object {
        const val DATE_RANGE_QUERY_STRATEGY_KEY = "date_range_query_strategy"
    }
}