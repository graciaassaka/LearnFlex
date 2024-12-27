package org.example.shared.domain.constant

import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * Enum class representing the data collections.
 *
 * @property value The name of the Firestore collection.
 */
enum class Collection(override val value: String) : ValuableEnum<String> {
    TEST("test"),
    PROFILES("profiles"),
    CURRICULA("curricula"),
    MODULES("modules"),
    LESSONS("lessons"),
    SECTIONS("sections"),
    SESSIONS("sessions")
}