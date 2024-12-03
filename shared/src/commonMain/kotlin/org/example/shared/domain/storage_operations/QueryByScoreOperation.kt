package org.example.shared.domain.storage_operations

import kotlinx.coroutines.flow.Flow
import org.example.shared.domain.model.definition.ScoreQueryable

/**
 * Interface representing a query operation that retrieves a set of IDs by a minimum score.
 *
 * @param Model The type of the model.
 */
interface QueryByScoreOperation<Model : ScoreQueryable> {
    /**
     * Retrieves a set of IDs that have a score greater than or equal to the given minimum score.
     *
     * @param parentId The ID of the parent entity.
     * @param minScore The minimum score to filter by.
     * @return A Flow containing the result of the query.
     */
    fun getIdsByMinScore(parentId: String, minScore: Int): Flow<Result<Set<String>>>
}