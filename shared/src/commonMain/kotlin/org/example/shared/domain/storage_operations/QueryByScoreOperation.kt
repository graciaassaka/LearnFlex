package org.example.shared.domain.storage_operations

import org.example.shared.domain.model.interfaces.ScoreQueryable

/**
 * Interface representing a query operation that retrieves a set of IDs by a minimum score.
 *
 * @param Model The type of the model.
 */
interface QueryByScoreOperation<Model : ScoreQueryable> {
    /**
     * Retrieves a list of models based on the specified minimum score for a given parent entity.
     *
     * @param parentId The ID of the parent entity for which the models will be retrieved.
     * @param minScore The minimum score threshold to filter the models.
     * @return A [Result] containing a list of models that meet the minimum score criteria.
     */
    suspend fun getByMinScore(parentId: String, minScore: Int): Result<List<Model>>
}