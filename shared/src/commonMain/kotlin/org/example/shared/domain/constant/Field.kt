package org.example.shared.domain.constant

import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * Enum class representing different fields of learning.
 *
 * @property value The name of the field of learning.
 */
@Suppress("unused")
enum class Field(override val value: String) : ValuableEnum<String> {
    ARTS("Arts"),
    BUSINESS("Business"),
    COMPUTER_SCIENCE("Computer Science"),
    ENGINEERING("Engineering"),
    HEALTH("Health"),
    HUMANITIES("Humanities"),
    LANGUAGES("Languages"),
    LAW("Law"),
    MATH("Math"),
    SCIENCE("Science"),
    SOCIAL_SCIENCE("Social Science")
}

