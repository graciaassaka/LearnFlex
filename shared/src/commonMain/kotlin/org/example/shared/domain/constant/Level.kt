package org.example.shared.domain.constant

import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * Enum class representing different levels of learning.
 */
@Suppress("unused")
enum class Level(override val value: String) : ValuableEnum<String> {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}