package org.example.shared.domain.use_case.path

import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * Use case for building a section path.
 *
 * @property pathBuilder The PathBuilder instance used to build the path.
 */
class BuildSectionPathUseCase(private val pathBuilder: PathBuilder) {

    /**
     * Builds a section path using the provided identifiers.
     *
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @param moduleId The ID of the module.
     * @param lessonId The ID of the lesson.
     * @return The built section path.
     * @throws IllegalArgumentException if any of the provided IDs are blank.
     */
    operator fun invoke(userId: String, curriculumId: String, moduleId: String, lessonId: String): String {
        require(userId.isNotBlank()) { "User ID must not be blank." }
        require(curriculumId.isNotBlank()) { "Curriculum ID must not be blank." }
        require(moduleId.isNotBlank()) { "Module ID must not be blank." }
        require(lessonId.isNotBlank()) { "Lesson ID must not be blank." }

        return pathBuilder.buildSectionPath(userId, curriculumId, moduleId, lessonId)
    }
}