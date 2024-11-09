package org.example.shared.domain.repository

import org.example.shared.data.model.StyleResult

/**
 * Repository interface for managing learning styles.
 */
interface LearningStyleRepos {

    /**
     * Retrieves the learning style for the given ID.
     * @param id The ID of the learning style to retrieve.
     * @return A [Result] containing the [StyleResult] or an error.
     */
    suspend fun getLearningStyle(id: String): Result<StyleResult>

    /**
     * Sets the learning style for the given ID.
     * @param id The ID of the learning style to set.
     * @param style The [StyleResult] to set.
     * @return A [Result] indicating success or failure.
     */
    suspend fun setLearningStyle(id: String, style: StyleResult): Result<Unit>

    /**
     * Deletes the learning style for the given ID.
     * @param id The ID of the learning style to delete.
     * @return A [Result] indicating success or failure.
     */
    suspend fun deleteLearningStyle(id: String): Result<Unit>
}