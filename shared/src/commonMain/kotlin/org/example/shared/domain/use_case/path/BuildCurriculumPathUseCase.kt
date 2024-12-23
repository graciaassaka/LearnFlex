package org.example.shared.domain.use_case.path

import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for building a curriculum path.
 *
 * @property pathBuilder The PathBuilder instance used to build the curriculum path.
 */
class BuildCurriculumPathUseCase(private val pathBuilder: PathBuilder) {

    /**
     * Builds the curriculum path for the given user ID.
     *
     * @param userId The ID of the user for whom the curriculum path is to be built.
     * @return The curriculum path as a String.
     * @throws IllegalArgumentException if the user ID is blank.
     */
    operator fun invoke(userId: String): String {
        require(userId.isNotBlank()) { "User ID must not be blank." }

        return pathBuilder.buildCurriculumPath(userId)
    }
}