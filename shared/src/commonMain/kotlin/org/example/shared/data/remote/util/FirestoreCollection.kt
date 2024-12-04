package org.example.shared.data.remote.util

/**
 * Enum representing Firestore collections.
 *
 * @property value The name of the Firestore collection.
 */
enum class FirestoreCollection(val value: String) {
    USERS("users")
}

enum class FirestoreSubCollection(val value: String) {
    CURRICULUMS("curriculums"),
    MODULES("modules"),
    LESSONS("lessons"),
    SECTIONS("sections"),
    SESSIONS("sessions")
}