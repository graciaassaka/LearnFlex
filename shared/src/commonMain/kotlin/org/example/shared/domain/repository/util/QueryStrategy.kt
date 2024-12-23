package org.example.shared.domain.repository.util

import kotlinx.coroutines.flow.Flow

/**
 * A strategy interface for executing queries that return a flow of nullable results.
 *
 * @param T the type of the result.
 */
interface QueryStrategy<T> {
    /**
     * Executes the query and returns a flow of nullable results.
     *
     * @return a [Flow] emitting nullable results of type [T].
     */
    fun execute(): Flow<T?>
}