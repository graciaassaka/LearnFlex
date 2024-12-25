package org.example.shared.data.remote.firestore

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.storage_operations.util.PathBuilder

/**
 * FirestorePathBuilder is responsible for constructing paths for various Firestore collections and documents
 * related to users, curriculums, modules, lessons, sections, and sessions.
 * It implements the PathBuilder interface and uses FirestorePathConstructor to build the paths.
 */
class FirestorePathBuilder : PathBuilder {
    /**
     * Constructs and returns the Firestore path for the profiles collection.
     *
     * @return A string representing the path to the profiles collection in Firestore.
     */
    override fun buildProfilePath() = FirestorePathConstructor()
        .collection(Collection.PROFILES.value)
        .build()

    /**
     * Constructs a Firestore path to access the curriculums of a specific user.
     *
     * @param profileId The ID of the user whose curriculum path is to be built.
     * @return The constructed Firestore path as a string.
     */
    override fun buildCurriculumPath(profileId: String) = FirestorePathConstructor()
        .collection(Collection.PROFILES.value)
        .document(profileId)
        .collection(Collection.CURRICULA.value)
        .build()

    /**
     * Constructs a Firestore path for accessing the modules within a specific curriculum for a user.
     *
     * @param userId The ID of the user whose curriculum modules are being accessed.
     * @param curriculumId The ID of the curriculum whose modules are being accessed.
     * @return The Firestore path to the modules as a string.
     */
    override fun buildModulePath(userId: String, curriculumId: String) = FirestorePathConstructor()
        .collection(Collection.PROFILES.value)
        .document(userId)
        .collection(Collection.CURRICULA.value)
        .document(curriculumId)
        .collection(Collection.MODULES.value)
        .build()

    /**
     * Constructs the Firestore path for a lesson document.
     *
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @param moduleId The ID of the module.
     * @return The Firestore path as a string.
     */
    override fun buildLessonPath(userId: String, curriculumId: String, moduleId: String) = FirestorePathConstructor()
        .collection(Collection.PROFILES.value)
        .document(userId)
        .collection(Collection.CURRICULA.value)
        .document(curriculumId)
        .collection(Collection.MODULES.value)
        .document(moduleId)
        .collection(Collection.LESSONS.value)
        .build()

    /**
     * Constructs a Firestore path for accessing a specific section within a lesson module associated with a user and curriculum.
     *
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @param moduleId The ID of the module within the curriculum.
     * @param lessonId The ID of the lesson within the module.
     * @return A constructed Firestore path as a string targeting the sections collection under the specified lesson.
     */
    override fun buildSectionPath(userId: String, curriculumId: String, moduleId: String, lessonId: String) =
        FirestorePathConstructor()
            .collection(Collection.PROFILES.value)
            .document(userId)
            .collection(Collection.CURRICULA.value)
            .document(curriculumId)
            .collection(Collection.MODULES.value)
            .document(moduleId)
            .collection(Collection.LESSONS.value)
            .document(lessonId)
            .collection(Collection.SECTIONS.value)
            .build()

    /**
     * Constructs a Firestore path for accessing session documents for a specific user.
     *
     * @param profileId The ID of the user for whom the session path is being built.
     * @return The complete Firestore path as a string that points to the sessions sub-collection
     *         of the specified user.
     */
    override fun buildSessionPath(profileId: String) = FirestorePathConstructor()
        .collection(Collection.PROFILES.value)
        .document(profileId)
        .collection(Collection.SESSIONS.value)
        .build()
}