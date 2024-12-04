package org.example.shared.data.remote.firestore

import org.example.shared.data.remote.util.FirestoreCollection
import org.example.shared.data.remote.util.FirestoreSubCollection
import org.example.shared.domain.dao.util.PathBuilder

/**
 * FirestorePathBuilder is responsible for constructing paths for various Firestore collections and documents
 * related to users, curriculums, modules, lessons, sections, and sessions.
 * It implements the PathBuilder interface and uses FirestorePathConstructor to build the paths.
 */
class FirestorePathBuilder : PathBuilder {
    /**
     * Constructs and returns the Firestore path for the users collection.
     *
     * @return A string representing the path to the users collection in Firestore.
     */
    override fun buildUserPath() = FirestorePathConstructor()
        .collection(FirestoreCollection.USERS.value)
        .build()

    /**
     * Constructs a Firestore path to access the curriculums of a specific user.
     *
     * @param userId The ID of the user whose curriculum path is to be built.
     * @return The constructed Firestore path as a string.
     */
    override fun buildCurriculumPath(userId: String) = FirestorePathConstructor()
        .collection(FirestoreCollection.USERS.value)
        .document(userId)
        .collection(FirestoreSubCollection.CURRICULUMS.value)
        .build()

    /**
     * Constructs a Firestore path for accessing the modules within a specific curriculum for a user.
     *
     * @param userId The ID of the user whose curriculum modules are being accessed.
     * @param curriculumId The ID of the curriculum whose modules are being accessed.
     * @return The Firestore path to the modules as a string.
     */
    override fun buildModulePath(userId: String, curriculumId: String) = FirestorePathConstructor()
        .collection(FirestoreCollection.USERS.value)
        .document(userId)
        .collection(FirestoreSubCollection.CURRICULUMS.value)
        .document(curriculumId)
        .collection(FirestoreSubCollection.MODULES.value)
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
        .collection(FirestoreCollection.USERS.value)
        .document(userId)
        .collection(FirestoreSubCollection.CURRICULUMS.value)
        .document(curriculumId)
        .collection(FirestoreSubCollection.MODULES.value)
        .document(moduleId)
        .collection(FirestoreSubCollection.LESSONS.value)
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
            .collection(FirestoreCollection.USERS.value)
            .document(userId)
            .collection(FirestoreSubCollection.CURRICULUMS.value)
            .document(curriculumId)
            .collection(FirestoreSubCollection.MODULES.value)
            .document(moduleId)
            .collection(FirestoreSubCollection.LESSONS.value)
            .document(lessonId)
            .collection(FirestoreSubCollection.SECTIONS.value)
            .build()

    /**
     * Constructs a Firestore path for accessing session documents for a specific user.
     *
     * @param userId The ID of the user for whom the session path is being built.
     * @return The complete Firestore path as a string that points to the sessions sub-collection
     *         of the specified user.
     */
    override fun buildSessionPath(userId: String) = FirestorePathConstructor()
        .collection(FirestoreCollection.USERS.value)
        .document(userId)
        .collection(FirestoreSubCollection.SESSIONS.value)
        .build()
}