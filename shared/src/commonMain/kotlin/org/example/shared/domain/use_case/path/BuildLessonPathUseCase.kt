package org.example.shared.domain.use_case.path

import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for building a lesson path.
 *
 * @property pathBuilder The PathBuilder instance used to build the lesson path.
 */
class BuildLessonPathUseCase(private val pathBuilder: PathBuilder) {

    /**
     * Builds the lesson path using the provided IDs.
     *
     * @param userId The user ID.
     * @param curriculumId The curriculum ID.
     * @param moduleId The module ID.
     * @return The built lesson path.
     * @throws IllegalArgumentException if any of the IDs are blank.
     */
    operator fun invoke(userId: String, curriculumId: String, moduleId: String): String {
        require(userId.isNotBlank()) { "User ID cannot be blank." }
        require(curriculumId.isNotBlank()) { "Curriculum ID cannot be blank." }
        require(moduleId.isNotBlank()) { "Module ID cannot be blank." }

        return pathBuilder.buildLessonPath(userId, curriculumId, moduleId)
    }
}