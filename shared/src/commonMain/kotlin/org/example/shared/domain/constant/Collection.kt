package org.example.shared.domain.constant

/**
 * Enum class representing the data collections.
 *
 * @property value The name of the Firestore collection.
 */
enum class Collection(val value: String) {
    TEST("test"),
    PROFILES("profiles"),
    CURRICULA("curricula"),
    MODULES("modules"),
    LESSONS("lessons"),
    SECTIONS("sections"),
    SESSIONS("sessions")
}