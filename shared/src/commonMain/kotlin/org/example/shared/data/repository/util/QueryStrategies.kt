package org.example.shared.data.repository.util

import kotlinx.coroutines.flow.Flow
import org.example.shared.data.local.entity.definition.RoomEntity
import org.example.shared.domain.repository.util.QueryStrategy

/**
 * A class that holds various query strategies for entities.
 *
 * @param Entity The type of the entity that extends RoomEntity.
 */
class QueryStrategies<Entity : RoomEntity> {
    private var byIdStrategy: SingleEntityStrategyHolder<Entity>? = null
    private var allStrategy: CollectionStrategyHolder<Entity>? = null
    private val customStrategies = mutableMapOf<String, QueryStrategy<*>>()

    /**
     * A holder for a single entity query strategy.
     *
     * @param T The type of the entity.
     * @property strategy The query strategy function.
     */
    class SingleEntityStrategyHolder<T>(private val strategy: (String) -> Flow<T?>) : QueryStrategy<T> {
        private var id: String? = null

        /**
         * Sets the ID for the query.
         *
         * @param newId The new ID to set.
         */
        fun setId(newId: String) {
            id = newId
        }

        /**
         * Executes the query with the set ID.
         *
         * @return A Flow containing the result of the query.
         * @throws IllegalArgumentException if the ID is not set.
         */
        override fun execute(): Flow<T?> {
            requireNotNull(id) { "ID must be set before executing query" }
            return strategy(id!!)
        }
    }

    /**
     * A holder for a collection query strategy.
     *
     * @param T The type of the entity.
     * @property strategy The query strategy function.
     */
    class CollectionStrategyHolder<T>(private val strategy: (String?) -> Flow<List<T>>) : QueryStrategy<List<T>> {
        private var parentId: String? = null

        /**
         * Sets the parent ID for the query.
         *
         * @param newParentId The new parent ID to set.
         */
        fun setParentId(newParentId: String?) {
            parentId = newParentId
        }

        /**
         * Executes the query with the set parent ID.
         *
         * @return A Flow containing the result of the query.
         */
        override fun execute(): Flow<List<T>> = strategy(parentId)
    }

    /**
     * Configures a strategy for getting an entity by ID.
     *
     * @param strategy The query strategy function.
     */
    fun withGetById(strategy: (String) -> Flow<Entity?>) {
        byIdStrategy = SingleEntityStrategyHolder(strategy)
    }

    /**
     * Configures a strategy for getting all entities.
     *
     * @param strategy The query strategy function.
     */
    fun withGetAll(strategy: (String?) -> Flow<List<Entity>>) {
        allStrategy = CollectionStrategyHolder(strategy)
    }

    /**
     * Configures a custom query strategy.
     *
     * @param T The type of the result.
     * @param key The key to identify the custom strategy.
     * @param strategy The custom query strategy function.
     */
    fun <T> withCustomQuery(key: String, strategy: QueryStrategy<T>) {
        customStrategies[key] = strategy
    }

    /**
     * Retrieves the configured strategy for getting an entity by ID.
     *
     * @return The SingleEntityStrategyHolder.
     * @throws IllegalStateException if the strategy is not configured.
     */
    fun getByIdStrategy(): SingleEntityStrategyHolder<Entity> =
        byIdStrategy ?: error("GetById strategy not configured")

    /**
     * Retrieves the configured strategy for getting all entities.
     *
     * @return The CollectionStrategyHolder.
     * @throws IllegalStateException if the strategy is not configured.
     */
    fun getAllStrategy(): CollectionStrategyHolder<Entity> =
        allStrategy ?: error("GetAll strategy not configured")

    /**
     * Retrieves a configured custom query strategy.
     *
     * @param T The type of the result.
     * @param key The key to identify the custom strategy.
     * @return The custom query strategy.
     * @throws IllegalStateException if the strategy is not configured.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCustomStrategy(key: String): QueryStrategy<T> =
        customStrategies[key] as? QueryStrategy<T>
            ?: error("Custom strategy '$key' not configured")
}