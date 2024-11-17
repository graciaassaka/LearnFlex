package org.example.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * A data class representing learning preferences.
 *
 * @property field The field of study.
 * @property level The level of expertise.
 * @property goal The learning goal.
 */
@Serializable
data class LearningPreferences(
    val field: String,
    val level: String,
    val goal: String
)

/**
 * Enum class representing different fields of learning.
 */
@Suppress("unused")
enum class Field {
    Arts,
    Business,
    ComputerScience,
    Engineering,
    Health,
    Humanities,
    Languages,
    Law,
    Math,
    Science,
    SocialScience
}

/**
 * Enum class representing different levels of learning.
 */
@Suppress("unused")
enum class Level {
    Beginner,
    Intermediate,
    Advanced
}