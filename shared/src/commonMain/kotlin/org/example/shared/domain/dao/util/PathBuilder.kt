package org.example.shared.domain.dao.util

/**
 * Interface for constructing various types of paths related to user and educational resources.
 */
interface PathBuilder {
    /**
     * Constructs and returns a file path string for a user directory.
     *
     * @return A string representing the path to the user's directory.
     */
    fun buildUserPath(): String

    /**
     * Constructs the path for accessing a user's curriculum.
     *
     * @param userId The unique identifier of the user.
     * @return The path as a string for the specified user's curriculum.
     */
    fun buildCurriculumPath(userId: String): String

    /**
     * Constructs a module path based on the provided user and curriculum IDs.
     *
     * @param userId the unique identifier of the user for whom the path is being built
     * @param curriculumId the unique identifier of the curriculum within the user's profile
     * @return a string representing the path to the specified module
     */
    fun buildModulePath(userId: String, curriculumId: String): String

    /**
     * Constructs a path string representing the location of a lesson within a specified user's curriculum module.
     *
     * @param userId The identifier for the user.
     * @param curriculumId The identifier for the curriculum to which the module belongs.
     * @param moduleId The identifier for the module containing the lesson.
     * @return A string representing the path to the lesson within the specified module.
     */
    fun buildLessonPath(userId: String, curriculumId: String, moduleId: String): String

    /**
     * Constructs a path string that uniquely identifies a section within a lesson, module, curriculum, and user hierarchy.
     *
     * @param userId the unique identifier of the user
     * @param curriculumId the unique identifier of the curriculum
     * @param moduleId the unique identifier of the module
     * @param lessonId the unique identifier of the lesson
     * @return a string representing the complete path to a specific section
     */
    fun buildSectionPath(userId: String, curriculumId: String, moduleId: String, lessonId: String): String

    /**
     * Constructs the path for a user session based on their user ID.
     *
     * @param userId The unique identifier for the user.
     * @return A string representing the path to the user's session.
     */
    fun buildSessionPath(userId: String): String
}