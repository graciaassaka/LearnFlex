package org.example.shared.domain.use_case.path

import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for building a module path.
 *
 * @property pathBuilder The PathBuilder instance used to build the path.
 */
class BuildModulePathUseCase(private val pathBuilder: PathBuilder) {

    /**
     * Builds the module path for a given user and curriculum.
     *
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @return The built module path.
     * @throws IllegalArgumentException if userId or curriculumId is blank.
     */
    operator fun invoke(userId: String, curriculumId: String): String {
        require(userId.isNotBlank()) { "User ID cannot be blank." }
        require(curriculumId.isNotBlank()) { "Curriculum ID cannot be blank." }

        return pathBuilder.buildModulePath(userId, curriculumId)
    }
}