package org.example.shared.domain.use_case.path

import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for building a user session path.
 *
 * @property pathBuilder The PathBuilder instance used to construct the session path.
 */
class BuildSessionPathUseCase(private val pathBuilder: PathBuilder) {
    /**
     * Builds the session path for a given user ID.
     *
     * @param userId The unique identifier for the user. Must not be blank.
     * @return A string representing the session path for the user.
     * @throws IllegalArgumentException if the user ID is blank.
     */
    operator fun invoke(userId: String): String {
        require(userId.isNotBlank()) { "User ID must not be blank." }

        return pathBuilder.buildSessionPath(userId)
    }
}